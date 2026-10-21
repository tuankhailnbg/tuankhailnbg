//package nro.netty;
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.util.concurrent.Future;
//import nro.server.Manager;
//
//public class NettyServer extends Thread {
//
//    private EventLoopGroup bossGroup;
//    private EventLoopGroup workerGroup;
//    private Channel serverChannel;
//
//    @Override
//    public void run() {
//        try {
//            bossGroup = new NioEventLoopGroup(Manager.bossGroup);
//            workerGroup = new NioEventLoopGroup(Manager.workerGroup);
//            try {
//                ServerBootstrap bootstrap = new ServerBootstrap();
//                bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
//                bootstrap.group(bossGroup, workerGroup)
//                        .channel(NioServerSocketChannel.class)
//                        .childHandler(new HttpServerInitializer());
//
//                serverChannel = bootstrap.bind(Manager.apiPort).sync().channel();
//                serverChannel.closeFuture().sync();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                shutdown();
//            }
//        } finally {
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//        }
//    }
//
//    public void shutdown() {
//        if (serverChannel != null) {
//            serverChannel.close();
//        }
//        if (bossGroup != null && workerGroup != null) {
//            try {
//                Future<?> bossGroupFuture = bossGroup.shutdownGracefully();
//                Future<?> workerGroupFuture = workerGroup.shutdownGracefully();
//                bossGroupFuture.await();
//                workerGroupFuture.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//}
//
