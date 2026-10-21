//package nro.netty;
//
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.handler.codec.http.HttpRequestDecoder;
//import io.netty.handler.codec.http.HttpResponseEncoder;
//
//public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
//
//    @Override
//    public void initChannel(SocketChannel ch) {
//        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast("decoder", new HttpRequestDecoder());
//        pipeline.addLast("encoder", new HttpResponseEncoder());
//        pipeline.addLast("handler", new HttpRequestHandler());
//    }
//}