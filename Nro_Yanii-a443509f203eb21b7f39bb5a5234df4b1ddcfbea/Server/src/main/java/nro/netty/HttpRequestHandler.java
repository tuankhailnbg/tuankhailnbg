//package nro.netty;
//
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.http.*;
//import nro.consts.ConstAdminCommand;
//import nro.models.item.Item;
//import nro.models.item.ItemOption;
//import nro.models.player.Player;
//import nro.server.AutoMaintenance;
//import nro.server.Client;
//import nro.server.Maintenance;
//import nro.server.Manager;
//import nro.services.*;
//import org.json.simple.JSONObject;
//
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> {
//
//    private Map<Integer, Integer> options = new HashMap<>();
//    private static Player player;
//
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.flush();
//    }
//
//    @Override
//    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
//        if (msg instanceof HttpRequest req) {
//            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
//            Map<String, List<String>> params = queryStringDecoder.parameters();
//            String rep = "Hello World";
//            String method = String.valueOf(req.getMethod());
//            if (!method.equals("OPTIONS")) {
//                if (!params.isEmpty()) {
//                    rep = handler(params).toJSONString();
//                }
//            }
//
//            boolean keepAlive = HttpUtil.isKeepAlive(req);
//            FullHttpResponse response = null;
//            response = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK,
//                    Unpooled.wrappedBuffer(rep.getBytes(StandardCharsets.UTF_8)));
//            response.headers()
//                    .set("Access-Control-Allow-Origin", "*")
//                    .set("Access-Control-Allow-Methods", "GET,POST")
//                    .set("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Content-Length")
//                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
//                    .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
//
//            if (keepAlive) {
//                if (!req.protocolVersion().isKeepAliveDefault()) {
//                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//                }
//            } else {
//                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//            }
//
//            ChannelFuture f = ctx.write(response);
//
//            if (!keepAlive) {
//                f.addListener(ChannelFutureListener.CLOSE);
//            }
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        ctx.close();
//    }
//
//    public JSONObject handler(Map<String, List<String>> params) {
//        try {
//            if (params.containsKey("key")) {
//                String key = params.get("key").get(0);
//                if (key.equals(Manager.apiKey)) {
//                    if (params.containsKey("type")) {
//                        String type = params.get("type").get(0);
//                        switch (type) {
//                            case ConstAdminCommand.ADD_GOLD: {
//                                if (params.containsKey("amount")) {
//                                    int amount = Integer.parseInt(params.get("amount").get(0));
//                                    return addGoldBar(amount);
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            }
//                            case ConstAdminCommand.ADD_RUBY: {
//                                if (params.containsKey("amount")) {
//                                    int amount = Integer.parseInt(params.get("amount").get(0));
//                                    return addRuby(amount);
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            }
//                            case ConstAdminCommand.ADD_ITEM: {
//                                if (params.containsKey("item_id") && params.containsKey("amount") &&
//                                        params.containsKey("addoption") && params.containsKey("uptoup")) {
//                                    short itemID = Short.parseShort(params.get("item_id").get(0));
//                                    int amount = Integer.parseInt(params.get("amount").get(0));
//                                    boolean upToUp = Boolean.parseBoolean(params.get("uptoup").get(0));
//                                    boolean addOptionInList = Boolean.parseBoolean(params.get("addoption").get(0));
//                                    return addItem(itemID, amount, upToUp, addOptionInList);
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            }
//                            case ConstAdminCommand.ONLINE:
//                                return createMessage("succes", String.valueOf(Client.gI().getSessions().size()));
//                            case ConstAdminCommand.BAN:
//                                if (params.containsKey("player_id")) {
//                                    return ban(Integer.parseInt(params.get("player_id").get(0)));
//                                } else if (params.containsKey("player_name")) {
//                                    return ban(params.get("player_name").get(0));
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            case ConstAdminCommand.SET_PLAYER:
//                                if (params.containsKey("player_name")) {
//                                    return setPlayer(params.get("player_name").get(0));
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            case ConstAdminCommand.ADD_OPTION:
//                                if (params.containsKey("option_id") && params.containsKey("param")) {
//                                    int optionID = Integer.parseInt(params.get("option_id").get(0));
//                                    int param = Integer.parseInt(params.get("param").get(0));
//                                    return addOptionToList(optionID, param);
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            case ConstAdminCommand.CLEAR_OPTION:
//                                if (params.containsKey("index")) {
//                                    int index = Integer.parseInt(params.get("index").get(0));
//                                    return clearOption(index);
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            case ConstAdminCommand.GET_ITEM_BAG:
//                                return getListItemBags();
//                            case ConstAdminCommand.CLEAR_LIST_OPTION:
//                                return clearList();
//                            case ConstAdminCommand.REMOVE_ITEM:
//                                if (params.containsKey("index")) {
//                                    int index = Integer.parseInt(params.get("index").get(0));
//                                    return removeItemBag(index);
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            case ConstAdminCommand.ADD_OPTIONS_TO_ITEM:
//                                if (params.containsKey("index") && params.containsKey("clear")) {
//                                    int index = Integer.parseInt(params.get("index").get(0));
//                                    boolean clear = Boolean.parseBoolean(params.get("clear").get(0));
//                                    return addOptionsInListToItem(index, clear);
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            case ConstAdminCommand.GET_INFO_PLAYER:
//                                return getInfoPlayer();
//                            case ConstAdminCommand.SEND_NOTI:
//                                if (params.containsKey("content")) {
//                                    return sendNoti(params.get("content").get(0));
//                                } else {
//                                    return createMessage("error", "Please enter all fields!");
//                                }
//                            case ConstAdminCommand.RESTART:
//                                new Thread(() -> new AutoMaintenance().execute()).start();
//                                return createMessage("success", "Hệ thống sẽ khởi động lại sau 60 giây!");
//                            case ConstAdminCommand.MAINTENANCE:
//                                new Thread(() -> Maintenance.gI().start(5)).start();
//                                return createMessage("success", "Máy chủ sẽ bảo trì sau 5 giây!");
//                        }
//                    } else {
//                        return createMessage("error", "Type parameter not found!");
//                    }
//                } else {
//                    return createMessage("error", "Key is not correct!");
//                }
//            } else {
//                return createMessage("error", "Key parameter not found!");
//            }
//        } catch (Exception e) {
//            return createMessage("error", e.getMessage());
//        }
//        return createMessage("error", "hmm");
//    }
//
//    private JSONObject ban(int playerID) {
//        Player player = Client.gI().getPlayer(playerID);
//        if (player == null) {
//            return createMessage("error", "Người chơi không tồn tại hoặc không online!");
//        }
//        PlayerService.gI().banPlayer(player);
//        player.getSession().disconnect();
//        return createMessage("success", "Khóa tài khoản thành công!");
//    }
//
//    private JSONObject ban(String playerName) {
//        Player player = Client.gI().getPlayer(playerName);
//        if (player == null) {
//            return createMessage("error", "Người chơi không tồn tại hoặc không online!");
//        }
//        PlayerService.gI().banPlayer(player);
//        player.getSession().disconnect();
//        return createMessage("success", "Khóa tài khoản thành công!");
//    }
//
//    public JSONObject createMessage(String status, String message) {
//        JSONObject obj = new JSONObject();
//        obj.put("status", status);
//        obj.put("message", message);
//        return obj;
//    }
//
//    public JSONObject addGoldBar(int quantity) {
//        Item item = ItemService.gI().createNewItem((short) 457);
//        item.quantity = quantity;
//        InventoryService.gI().addItemBag(this.player, item, 0);
//        InventoryService.gI().sendItemBags(this.player);
//        Service.getInstance().sendThongBao(this.player, "Bạn nhận được " + quantity + " Thỏi Vàng");
//        return createMessage("success", "Thành công!");
//    }
//
//    public JSONObject addRuby(int quantity) {
//        this.player.inventory.ruby += quantity;
//        PlayerService.gI().sendInfoHpMpMoney(this.player);
//        Service.getInstance().sendThongBao(this.player, "Bạn nhận được " + quantity + " Hồng Ngọc");
//        return createMessage("success", "Thành công!");
//    }
//
//    public JSONObject addGold(int quantity) {
//        this.player.inventory.addGold(quantity);
//        PlayerService.gI().sendInfoHpMpMoney(this.player);
//        Service.getInstance().sendThongBao(this.player, "Bạn nhận được " + quantity + " Vàng");
//        return createMessage("success", "Thêm thành công!");
//    }
//
//    public JSONObject addOptionToList(int optionID, int param) {
//        this.options.put(optionID, param);
//        return createMessage("success", "Thêm thành công!");
//    }
//
//    public JSONObject clearOption(int index) {
//        Item it = InventoryService.gI().findItemBagByIndex(this.player, index);
//        it.itemOptions.clear();
//        InventoryService.gI().sendItemBags(this.player);
//        return createMessage("success", "Clear options thành công!");
//    }
//
//    public JSONObject getListItemBags() {
//        return createMessage("success", InventoryService.gI().itemsBagToString(this.player));
//    }
//
//    public JSONObject clearList() {
//        this.options.clear();
//        return createMessage("success", "Thành công!");
//    }
//
//    public JSONObject addOptionsInListToItem(int index, boolean clearList) {
//        Item it = InventoryService.gI().findItemBagByIndex(this.player, index);
//        for (Map.Entry<Integer, Integer> o : this.options.entrySet()) {
//            it.itemOptions.add(new ItemOption(o.getKey(), o.getValue()));
//        }
//        InventoryService.gI().sendItemBags(this.player);
//        if (clearList) {
//            options.clear();
//        }
//        return createMessage("success", "Thành công!");
//    }
//
//    public JSONObject getInfoPlayer() {
//        JSONObject info = new JSONObject();
//        info.put("name", this.player.name);
//        info.put("ruby", this.player.inventory.ruby);
//        info.put("gender", this.player.gender);
//        return createMessage("success", info.toJSONString());
//    }
//
//    public JSONObject addItem(short itemID, int quantity, boolean upToUp, boolean addOptionsInList) {
//        Item item = ItemService.gI().createNewItem(itemID);
//        RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type, item.itemOptions);
//        if (addOptionsInList) {
//            for (Map.Entry<Integer, Integer> o : this.options.entrySet()) {
//                item.itemOptions.add(new ItemOption(o.getKey(), o.getValue()));
//            }
//        }
//        if (!upToUp) {
//            for (int i = 0; i < quantity; i++) {
//                InventoryService.gI().addItemBag(this.player, item, 0);
//            }
//        } else {
//            item.quantity = quantity;
//            InventoryService.gI().addItemBag(this.player, item, 0);
//        }
//        InventoryService.gI().sendItemBags(this.player);
//        Service.getInstance().sendThongBao(this.player, "Bạn nhận được " + item.template.name + " Số lượng: " + quantity);
//        return createMessage("success", "Thêm thành công!");
//    }
//
//    public JSONObject removeItemBag(int index) {
//        InventoryService.gI().throwItem(this.player, 1, index);
//        return createMessage("success", "Thành công!");
//    }
//
//    public JSONObject setPlayer(String name) {
//        this.player = Client.gI().getPlayer(name);
//        if (this.player == null) {
//            return createMessage("error", "Người chơi không tồn tại hoặc không online!");
//        }
//        return createMessage("success", "Thành công!");
//    }
//
//    public JSONObject sendNoti(String text) {
//        Service.getInstance().sendBigMessAllPlayer(1139, "|7|Thông Báo :\n" + text.replaceAll(";", "\n"));
//        return createMessage("success", "Thành công!");
//    }
//}
