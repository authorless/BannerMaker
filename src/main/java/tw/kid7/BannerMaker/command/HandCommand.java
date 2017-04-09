package tw.kid7.BannerMaker.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tw.kid7.BannerMaker.BannerMaker;
import tw.kid7.BannerMaker.util.BannerUtil;
import tw.kid7.BannerMaker.util.InventoryMenuUtil;
import tw.kid7.BannerMaker.util.MessageUtil;

class HandCommand extends AbstractCommand {
    //名稱
    private static final String name = "Hand";
    //介紹
    private static final String description = "Show banner info of the banner in hand";
    //權限
    private static final String permission = "BannerMaker.hand";
    //使用方法
    private static final String usage = "/bm hand";
    //僅能由玩家執行
    private static final boolean onlyFromPlayer = true;

    HandCommand() {
        super(name, description, permission, usage, onlyFromPlayer);
    }

    @Override
    boolean handle(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack itemStack = BannerMaker.getInstance().getVersionHandler().getItemInMainHand(player);
        if (!BannerUtil.isBanner(itemStack)) {
            //TODO: 加到語言包
            player.sendMessage(MessageUtil.format(true, "&cItem in hand is not a banner."));
            return true;
        }
        //設定查看旗幟
        //FIXME: 清理旗幟資訊（自訂名稱等）
        //顯示旗幟
        InventoryMenuUtil.showBannerInfo(player, itemStack);

        return true;
    }
}
