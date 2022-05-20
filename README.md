# x-netty-rpc
a rpc framework which base on netty 


基于netty的rpc框架

支持自定义协议，支持直接客户端运行或者集成spring运行，自定义处理器处理，简单易用

在分布式下载组件中已经投入使用x-netty-rpc

gitee地址：
https://gitee.com/lingfengx/x-downloader

git地址：
https://github.com/EditFly/x-downloader


## 自定义安全协议 SafeFrame

支持灵活更换序列化协议(JSON、JAVA、string、protobuf)

支持自动加密解密（AES、RSA） 

支持消息自动签名校验防篡改(MD5)  

自带时间戳 防重放攻击(timestamp) 



```

 * BEFORE DECODE (66 bytes)                                     			   AFTER DECODE (66 bytes)
 * +------+--------+------+------------------------------------------------+     +------+--------+------+-----------------------------------------------+   
 * | cmd | serial | encrypt | timestamp | client| sign |length|content(11)       | cmd | serial | encrypt | timestamp | client |sign|length|content(11)  
 * |   1 |    1   |    1    |     8     |    8  |  32  |  4   | "HELLO,WORLD"	 |   1 |    1    |    1   |     8     |    8   | 32  |  4  |"HELLO,WORLD"
 * +------+--------+------+------------------------------------------------+     +------+--------+------+-----------------------------------------------+  

    //帧类型     //请求REQUEST((byte) 1), //返回RESPONSE((byte) 2), //心跳HEARTBEAT((byte) 3);
    private byte cmd;
    //数据(content)序列化类型 JSON_SERIAL JAVA_SERIAL
    private byte serial;
    //加密类型 //明文NONE((byte) 0),//AES AES((byte) 2), //RSA RSA((byte) 3);
    private byte encrypt;
    //时间戳  相当于salt
    private long timestamp;
    //客户端id  -1代表服务端
    private long client;
    //消息签名 MD5 固定32位 
    private String sign;
    //content 长度
    private int length;
    //内容
    private T content;
```


## 客户端

支持自动重启自动连接

支持手动主动关闭连接

支持多线程发送消息

### 客户端接口
```
 NettyClient {

    int state();//客户端状态

    void start();//启动

    void restart();//重启

    void close();//关闭

    long getClientId();//获取客户端id

    void defaultChannel(Channel channel);//设置默认channel


    <M extends Serializable> void writeAndFlush(Channel channel, M msg, Cmd type);//发送消息

    <M extends Serializable> void writeAndFlush(ChannelHandlerContext channel, M msg, Cmd type);//发送消息
//     保留接口 <M extends Serializable> void writeAndFlush(M msg, Cmd type);
}
```

### 客户端demo
```

      BizNettyClient client = NettyClientFactory.buildBizNettyClient(new Address("127.0.0.1", 9999),
                () -> Arrays.asList(new NettyReqHandler()));//自定义 NettyReqHandler 消息处理器
        client.start();//启动

```

### 自定义注解 @RpcHandler 处理消息

服务端过来的消息，通过代理和反射进行远程RPC目标方法的执行
```

    @RpcHandler("complexParam")
    public Object complexParam(Map<String, Long> param) {
        Thread thread = Thread.currentThread();
        log.info(" client get a map data = {} ,thread={}", param, thread);

        return "map is OK  --bbq";
    }

```

### 自定义 NettyReqHandler 消息处理器
```
protected void channelRead0(ChannelHandlerContext ctx, SafeFrame<Frame<?>> data) throws Exception {
        byte cmd = data.getCmd();
        if (cmd == Cmd.REQUEST.code()) {
            Frame<?> frame = data.getContent();
            String name = frame.getTarget();
            //使用线程池处理任务
            getExecutor().execute(() -> {
                //代理执行方法
                RpcInvokeProxy.invoke(ret -> {
                    //返回数据
                    Frame<Object> resp = new Frame<>();
                    resp.setData(ret);
                    writeAndFlush(ctx.channel(), resp, Cmd.RESPONSE);

                }, name, frame.getData());
            });

        } else {
           // ctx.fireChannelRead(data);
        }
    }
```



## 服务端

支持关机重启

支持客户端管理

支持API方式发送消息、关闭客户端等

### 服务端接口
```
//服务器状态
    int state();
    //开启服务器
    void start();
    //重启服务器
    void restart();
    //停止服务器
    void stop();
    //获取服务器id
    long getServerId();

    //发送消息
    <M extends Serializable> void writeAndFlush(Channel channel, M msg, Cmd type);

    <M extends Serializable> void writeAndFlush(ChannelHandlerContext channel, M msg, Cmd type);

    //添加客户端channel
    void addChannel(String clientId, Channel channel);
    //关闭指定clientId的channel
    void closeChannel(String clientId);
    //获取所有的客户端channel
    Collection<Channel> allChannels();
    //获取指定clientId的客户端channel
    Channel findChanel(String clientId);
    //打印所有channel信息
    void showChannels();
```

### 服务端demo

```
BizNettyServer server =
                NettyServerFactory.buildBizNettyServer(
                        new Address("127.0.0.1", 9999),
                        () -> Arrays.asList(new NettyServerHandler()));
        server.start();
```

### 服务端处理器
```
@Override
    protected void channelRead0(ChannelHandlerContext ctx, SafeFrame<Frame<?>> data) throws Exception {
        byte cmd = data.getCmd();
        // request
        if (cmd == Cmd.REQUEST.code()) {
            Frame<?> frame = data.getContent();
            log.info("  server get REQUEST data = {}", frame);
            //返回数据
            // writeAndFlush(ctx.channel(), resp, Cmd.REQUEST);
        }
        //response
        if (cmd == Cmd.RESPONSE.code()) {
            Frame<?> frame = data.getContent();
            log.info("server get RESPONSE data = {}", frame);
        } else {
            //ctx.fireChannelRead(data);
        }
    }
```

## 序列化

### JAVA序列化
```
  public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(byteArrayOutputStream);

            outputStream.writeObject(obj);
            outputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream objectInputStream =
                    new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
```
### JSON序列化（Gson）
```
@Override
    public <T> byte[] serialize(T obj) {
        return GsonTool.toJson(obj).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return GsonTool.fromJson(new String(data), clazz);
    }
```




### 关于数据帧协议Frame解决的问题

## 1. LengthFieldBasedFrameDecoder作用
LengthFieldBasedFrameDecoder解码器自定义长度解决TCP粘包黏包问题。所以LengthFieldBasedFrameDecoder又称为: 自定义长度解码器

### 1.1 TCP粘包和黏包现象
- 1. TCP粘包是指发送方发送的若干个数据包到接收方时粘成一个包。从接收缓冲区来看，后一个包数据的头紧接着前一个数据的尾。
- 2. 当TCP连接建立后，Client发送多个报文给Server，TCP协议保证数据可靠性，但无法保证Client发了n个包，服务端也按照n个包接收。Client端发送n个数据包，Server端可能收到n-1或n+1个包。

### 1.2 为什么出现粘包现象

- 1. 发送方原因: TCP默认会使用Nagle算法。而Nagle算法主要做两件事：1）只有上一个分组得到确认，才会发送下一个分组；2）收集多个小分组，在一个确认到来时一起发送。所以，正是Nagle算法造成了发送方有可能造成粘包现象。
- 2. 接收方原因: TCP接收方采用缓存方式读取数据包，一次性读取多个缓存中的数据包。自然出现前一个数据包的尾和后一个收据包的头粘到一起。

### 1.3 如何解决粘包现象
- 1. 添加特殊符号，接收方通过这个特殊符号将接收到的数据包拆分开 - DelimiterBasedFrameDecoder特殊分隔符解码器
- 2. 每次发送固定长度的数据包 - FixedLengthFrameDecoder定长编码器
- 3. 在消息头中定义长度字段，来标识消息的总长度 - LengthFieldBasedFrameDecoder自定义长度解码器

## 2. LengthFieldBasedFrameDecoder怎么使用
- 1. LengthFieldBasedFrameDecoder本质上是ChannelHandler，一个处理入站事件的ChannelHandler
- 2. LengthFieldBasedFrameDecoder需要加入ChannelPipeline中，且位于链的头部

## 3. LengthFieldBasedFrameDecoder - 6个参数解释
LengthFieldBasedFrameDecoder是自定义长度解码器，所以构造函数中6个参数，基本都围绕那个定义长度域，进行的描述。

- 1. maxFrameLength - 发送的数据帧最大长度
- 2. lengthFieldOffset - 定义长度域位于发送的字节数组中的下标。换句话说：发送的字节数组中下标为${lengthFieldOffset}的地方是长度域的开始地方
- 3. lengthFieldLength - 用于描述定义的长度域的长度。换句话说：发送字节数组bytes时, 字节数组bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength]域对应于的定义长度域部分
- 4. lengthAdjustment - 满足公式: 发送的字节数组bytes.length - lengthFieldLength =  bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength] + lengthFieldOffset  + lengthAdjustment 
- 5. initialBytesToStrip - 接收到的发送数据包，去除前initialBytesToStrip位
- 6. failFast - true: 读取到长度域超过maxFrameLength，就抛出一个 TooLongFrameException。false: 只有真正读取完长度域的值表示的字节之后，才会抛出 TooLongFrameException，默认情况下设置为true，建议不要修改，否则可能会造成内存溢出
- 7. ByteOrder - 数据存储采用大端模式或小端模式
