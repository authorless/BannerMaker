package tw.kid7.BannerMaker.inventoryMenu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import tw.kid7.BannerMaker.BannerMaker;
import tw.kid7.BannerMaker.InventoryMenuState;
import tw.kid7.BannerMaker.PlayerData;
import tw.kid7.BannerMaker.PlayerDataMap;
import tw.kid7.BannerMaker.util.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static tw.kid7.BannerMaker.configuration.Language.tl;

public class BannerInfoInventoryMenu extends AbstractInventoryMenu {
    private static BannerInfoInventoryMenu instance = null;
    //按鈕位置
    private int buttonPositionPrevPage = 22;
    private int buttonPositionNextPage = 26;
    private int buttonPositionDelete = 47;
    private int buttonPositionGetBanner = 49;
    private int buttonPositionCloneAndEdit = 51;
    private int buttonPositionBackToMenu = 45;

    public static BannerInfoInventoryMenu getInstance() {
        if (instance == null) {
            instance = new BannerInfoInventoryMenu();
        }
        return instance;
    }

    @Override
    public void open(Player player) {
        PlayerData playerData = BannerMaker.getInstance().playerDataMap.get(player);
        //取得欲查看旗幟
        ItemStack banner = playerData.getViewInfoBanner();
        //僅限旗幟
        if (!BannerUtil.isBanner(banner)) {
            //回到主選單
            InventoryMenuUtil.openMenu(player, InventoryMenuState.MAIN_MENU);
            return;
        }
        //建立選單
        Inventory menu = InventoryMenuUtil.create(tl("gui.banner-info"));
        menu.setItem(0, banner);
        //patterns數量
        int patternCount = ((BannerMeta) banner.getItemMeta()).numberOfPatterns();
        String patternCountStr;
        if (patternCount > 0) {
            patternCountStr = patternCount + " " + tl("gui.pattern-s");
        } else {
            patternCountStr = tl("gui.no-patterns");
        }
        ItemStack signPatternCount;
        if (patternCount <= 6) {
            signPatternCount = new ItemBuilder(Material.SIGN).amount(1).name(MessageUtil.format("&a" + patternCountStr)).build();
        } else {
            signPatternCount = new ItemBuilder(Material.SIGN).amount(1).name(MessageUtil.format("&a" + patternCountStr)).lore(MessageUtil.format("&c" + tl("gui.uncraftable"))).build();
        }
        menu.setItem(1, signPatternCount);
        if (patternCount <= 6) {
            //材料是否充足
            ItemStack enoughMaterials;
            if (BannerUtil.hasEnoughMaterials(player.getInventory(), banner)) {
                enoughMaterials = new ItemBuilder(Material.SIGN).amount(1).name(MessageUtil.format("&a" + tl("gui.materials.enough"))).build();
            } else {
                enoughMaterials = new ItemBuilder(Material.SIGN).amount(1).name(MessageUtil.format("&c" + tl("gui.materials.not-enough"))).build();
            }
            menu.setItem(2, enoughMaterials);
            //材料清單
            List<Integer> materialPosition = Arrays.asList(9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39);
            List<ItemStack> materialList = BannerUtil.getMaterials(banner);
            for (int i = 0; i < materialList.size() && i < materialPosition.size(); i++) {
                ItemStack materialItem = materialList.get(i);
                int position = materialPosition.get(i);
                menu.setItem(position, materialItem);
            }

            //合成表
            //當前頁數
            int currentRecipePage = playerData.getCurrentRecipePage();
            //總頁數
            int totalPage = patternCount + 1;
            //外框
            ItemStack workbench = new ItemBuilder(Material.WORKBENCH).amount(currentRecipePage).name(MessageUtil.format("&a" + tl("gui.craft-recipe")))
                .lore(MessageUtil.format("(" + currentRecipePage + "/" + totalPage + ")")).build();
            menu.setItem(6, workbench);
            ItemStack border = new ItemBuilder(Material.STAINED_GLASS_PANE).amount(1).durability(12).name(" ").build();
            List<Integer> borderPosition = Arrays.asList(4, 5, 7, 8, 13, 17, 22, 26, 31, 35, 40, 41, 42, 43, 44);
            for (int i : borderPosition) {
                menu.setItem(i, border.clone());
            }
            //換頁按鈕
            //上一頁
            if (currentRecipePage > 1) {
                ItemStack prevPage = new ItemBuilder(Material.ARROW).amount(currentRecipePage - 1).name(MessageUtil.format("&a" + tl("gui.prev-page"))).build();
                menu.setItem(buttonPositionPrevPage, prevPage);
            }
            //下一頁
            if (currentRecipePage < totalPage) {
                ItemStack nextPage = new ItemBuilder(Material.ARROW).amount(currentRecipePage + 1).name(MessageUtil.format("&a" + tl("gui.next-page"))).build();
                menu.setItem(buttonPositionNextPage, nextPage);
            }
            //合成表
            HashMap<Integer, ItemStack> patternRecipe = BannerUtil.getPatternRecipe(banner, currentRecipePage);
            List<Integer> craftPosition = Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34, 42);
            for (int i = 0; i < 10; i++) {
                int position = craftPosition.get(i);
                ItemStack itemStack = patternRecipe.get(i);
                menu.setItem(position, itemStack);
            }
        }
        //新增按鈕
        //嘗試取得key
        String key = BannerUtil.getKey(banner);
        //刪除
        if (key != null) {
            //有KEY時（儲存於玩家資料時），才顯示刪除按鈕
            ItemStack btnDelete = new ItemBuilder(Material.BARRIER).amount(1).name(MessageUtil.format("&c" + tl("gui.delete"))).build();
            menu.setItem(buttonPositionDelete, btnDelete);
        }
        //取得旗幟
        if (player.hasPermission("BannerMaker.getBanner")) {
            //檢查是否啟用經濟
            ItemBuilder btnGetBannerBuilder = new ItemBuilder(Material.WOOL).amount(1).durability(5).name(MessageUtil.format("&a" + tl("gui.get-this-banner")));
            if (BannerMaker.getInstance().econ != null) {
                Double price = EconUtil.getPrice(banner);
                //FIXME 可能造成 IndexOutOfBoundsException: No group 1
                btnGetBannerBuilder.lore(MessageUtil.format("&a" + tl("gui.price", BannerMaker.getInstance().econ.format(price))));
            }
            ItemStack btnGetBanner = btnGetBannerBuilder.build();
            menu.setItem(buttonPositionGetBanner, btnGetBanner);
        }
        //複製並編輯
        ItemStack btnCloneAndEdit = new ItemBuilder(Material.BOOK_AND_QUILL).amount(1).name(MessageUtil.format("&9" + tl("gui.clone-and-edit"))).build();
        menu.setItem(buttonPositionCloneAndEdit, btnCloneAndEdit);

        //TODO 產生指令
        //返回
        ItemStack btnBackToMenu = new ItemBuilder(Material.WOOL).amount(1).durability(14).name(MessageUtil.format("&c" + tl("gui.back"))).build();
        menu.setItem(buttonPositionBackToMenu, btnBackToMenu);
        //開啟選單
        player.openInventory(menu);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = BannerMaker.getInstance().playerDataMap.get(player);
        int rawSlot = event.getRawSlot();
        if (rawSlot != 22 && rawSlot != 26 && rawSlot < 45) {
            return;
        }
        //取得欲查看旗幟
        ItemStack banner = playerData.getViewInfoBanner();
        //當前頁數
        int currentRecipePage = playerData.getCurrentRecipePage();
        //patterns數量
        int patternCount = ((BannerMeta) banner.getItemMeta()).numberOfPatterns();
        //總頁數
        int totalPage = patternCount + 1;
        //修改狀態
        if (rawSlot == buttonPositionPrevPage && currentRecipePage > 1) {
            playerData.setCurrentRecipePage(currentRecipePage - 1);
            InventoryMenuUtil.openMenu(player);
            return;
        }
        if (rawSlot == buttonPositionNextPage && currentRecipePage < totalPage) {
            playerData.setCurrentRecipePage(currentRecipePage + 1);
            InventoryMenuUtil.openMenu(player);
            return;
        }
        if (rawSlot == buttonPositionGetBanner) {
            //取得旗幟
            //嘗試給予玩家旗幟，並建立給予成功的標記
            boolean success = BannerUtil.give(player, banner);
            if (success) {
                //顯示名稱
                String showName = BannerUtil.getName(banner);
                //顯示訊息
                player.sendMessage(MessageUtil.format(true, "&a" + tl("gui.get-banner", showName)));
            }
            InventoryMenuUtil.openMenu(player);
            return;
        }
        if (rawSlot == buttonPositionCloneAndEdit) {
            //設定為編輯中旗幟
            playerData.setCurrentEditBanner(banner);
            InventoryMenuUtil.openMenu(player, InventoryMenuState.CREATE_BANNER);
            return;
        }
        if (rawSlot == buttonPositionDelete) {
            String key = BannerUtil.getKey(banner);
            if (key != null) {
                //有KEY時（儲存於玩家資料時），才能刪除
                IOUtil.removeBanner(player, key);
            }
            InventoryMenuUtil.openMenu(player, InventoryMenuState.MAIN_MENU);
            return;
        }
        if (rawSlot == buttonPositionBackToMenu) {
            //返回
            if (BannerUtil.isAlphabetBanner(banner)) {
                //若為Alphabet旗幟，回到Alphabet旗幟頁面
                InventoryMenuUtil.openMenu(player, InventoryMenuState.CREATE_ALPHABET);
                return;
            }
            InventoryMenuUtil.openMenu(player, InventoryMenuState.MAIN_MENU);
            return;
        }
    }
}
