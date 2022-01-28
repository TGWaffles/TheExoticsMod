package club.thom.tem.models.inventory.item;

import club.thom.tem.models.RarityConverter;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.nbt.NBTTagCompound;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ArmourPieceData extends InventoryItemData {
    private final NBTTagCompound itemData;
    public ArmourPieceData(NBTTagCompound itemData) {
        this.itemData = itemData;
    }

    @Override
    public ClientMessages.InventoryItem toInventoryItem() {
        // TODO:
        ClientMessages.Armour.Builder builder = ClientMessages.Armour.newBuilder();
        NBTTagCompound extraAttributes = getExtraAttributes();
        String itemId = extraAttributes.getString("id");
        builder.setItemId(itemId).setRarity(getRarity()).setReforge(getReforge()).setHexCode(getHexCode());
        return ClientMessages.InventoryItem.newBuilder().setUuid(extraAttributes.getString("uuid")).
                setArmourPiece(builder).build();
    }

    public long convertTimeStringToTimestamp() {
        String hypixelDateTimeString = getExtraAttributes().getString("timestamp") + " EST";
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm a z", Locale.US);
        Date date;
        try {
            date = format.parse(hypixelDateTimeString);
        } catch (ParseException e) {
            return -1;
        }
        return date.getTime();
    }

    private NBTTagCompound getExtraAttributes() {
        return itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes");
    }

    private ClientMessages.Rarity getRarity() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        String itemId = extraAttributes.getString("id");
        int upgrades = extraAttributes.getInteger("rarity_upgrades");
        ClientMessages.Rarity baseRarity = RarityConverter.getRarityFromItemId(itemId);
        assert baseRarity != null;
        for (int i = 0; i < upgrades; i++) {
            baseRarity = RarityConverter.levelUp(baseRarity);
        }
        return baseRarity;
    }

    private String getReforge() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        if (extraAttributes.hasKey("modifier")) {
            return extraAttributes.getString("modifier");
        }
        return "";
    }

    private String getHexCode() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        if (!extraAttributes.hasKey("color")) {
            return "undyed";
        }
        String[] colourArrayAsString = extraAttributes.getString("color").split(":");
        int[] colourArray = new int[3];
        for (int i = 0; i < 3; i++) {
            colourArray[i] = Integer.parseInt(colourArrayAsString[i]);
        }
        return convertIntArrayToHex(colourArray);
    }

    private String convertIntArrayToHex(int[] colourArray) {
        StringBuilder hexData = new StringBuilder();
        for (int colourValue : colourArray) {
            hexData.append(Integer.toHexString(colourValue));
        }
        return hexData.toString();
    }

    public static boolean isValidItem(NBTTagCompound itemData) {
        // I only care about leather armour here.
        short minecraftItemId = itemData.getShort("id");
        // 298, 299, 300, 301 is leather helm, chest, legs & boots
        return minecraftItemId > 297 && minecraftItemId < 302;
    }
}