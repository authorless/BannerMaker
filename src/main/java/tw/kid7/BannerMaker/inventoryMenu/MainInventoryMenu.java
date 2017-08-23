package tw.kid7.BannerMaker.inventoryMenu;

import club.kid7.pluginutilities.kitemstack.KItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tw.kid7.BannerMaker.BannerMaker;
import tw.kid7.BannerMaker.InventoryMenuState;
import tw.kid7.BannerMaker.PlayerData;
import tw.kid7.BannerMaker.util.AlphabetBanner;
import tw.kid7.BannerMaker.util.IOUtil;
import tw.kid7.BannerMaker.util.InventoryMenuUtil;
import tw.kid7.BannerMaker.util.MessageUtil;
import tw.kid7.util.customGUI.CustomGUIInventory;
import tw.kid7.util.customGUI.CustomGUIItemHandler;

import java.util.List;

import static tw.kid7.BannerMaker.configuration.Language.tl;

public class MainInventoryMenu extends AbstractInventoryMenu {
    private static MainInventoryMenu instance = null;

    public static MainInventoryMenu getInstance() {
        if (instance == null) {
            instance = new MainInventoryMenu();
        }
        return instance;
    }

    @Override
    public void open(final Player player) {
        final PlayerData playerData = BannerMaker.getInstance().playerDataMap.get(player);
        //建立選單
        String title = MessageUtil.format(tl("gui.prefix") + tl("gui.main-menu"));
        CustomGUIInventory menu = new CustomGUIInventory(title);
        //當前頁數
        final int currentPage = playerData.getCurrentPage();
        //顯示現有旗幟
        List<ItemStack> bannerList = IOUtil.loadBannerList(player, currentPage);
        for (int i = 0; i < bannerList.size() && i < 45; i++) {
            final ItemStack banner = bannerList.get(i);
            menu.setClickableItem(i, banner).set(ClickType.LEFT, new CustomGUIItemHandler() {
                @Override
                public void action() {
                    InventoryMenuUtil.showBannerInfo(player, banner);
                }
            });
        }
        //總頁數
        int totalPage = (int) Math.ceil(IOUtil.getBannerCount(player) / 45.0);
        //新增按鈕
        //換頁按鈕
        //上一頁
        if (currentPage > 1) {
            KItemStack prevPage = new KItemStack(Material.ARROW).amount(currentPage - 1).name(MessageUtil.format("&a" + tl("gui.prev-page")));
            menu.setClickableItem(45, prevPage).set(ClickType.LEFT, new CustomGUIItemHandler() {
                @Override
                public void action() {
                    playerData.setCurrentPage(currentPage - 1);
                    InventoryMenuUtil.openMenu(player);
                }
            });
        }
        //下一頁
        if (currentPage < totalPage) {
            KItemStack nextPage = new KItemStack(Material.ARROW).amount(currentPage + 1).name(MessageUtil.format("&a" + tl("gui.next-page")));
            menu.setClickableItem(53, nextPage).set(ClickType.LEFT, new CustomGUIItemHandler() {
                @Override
                public void action() {
                    playerData.setCurrentPage(currentPage + 1);
                    InventoryMenuUtil.openMenu(player);
                }
            });
        }
        //Create banner
        KItemStack btnCreateBanner = new KItemStack(Material.WOOL).amount(1).durability(5).name(MessageUtil.format("&a" + tl("gui.create-banner")));
        menu.setClickableItem(49, btnCreateBanner).set(ClickType.LEFT, new CustomGUIItemHandler() {
            @Override
            public void action() {
                InventoryMenuUtil.openMenu(player, InventoryMenuState.CREATE_BANNER);
            }
        });
        //建立字母
        if (BannerMaker.getInstance().enableAlphabetAndNumber) {
            ItemStack btnCreateAlphabet = AlphabetBanner.get("A");
            ItemMeta btnCreateAlphabetItemMeta = btnCreateAlphabet.getItemMeta();
            btnCreateAlphabetItemMeta.setDisplayName(MessageUtil.format("&a" + tl("gui.alphabet-and-number")));
            btnCreateAlphabet.setItemMeta(btnCreateAlphabetItemMeta);
            menu.setClickableItem(51, btnCreateAlphabet).set(ClickType.LEFT, new CustomGUIItemHandler() {
                @Override
                public void action() {
                    InventoryMenuUtil.openMenu(player, InventoryMenuState.CHOOSE_ALPHABET);
                }
            });
        }
        //開啟選單
        menu.open(player);
    }
}
