package media.sdp.base.attribute.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @class public class FmtpAttributeFactory extends AttributeFactory
 * @brief FmtpAttributeFactory class
 */
public class FmtpAttributeFactory extends AttributeFactory {

    public static final String MODE_SET = "mode-set";
    public static final String OCTET_ALIGN = "octet-align";

    List<Integer> modeSetList = new ArrayList<>();
    boolean isOaMode = false;

    public FmtpAttributeFactory(char type, String name, String value, List<String> mediaFormats) {
        super(type, name, value, mediaFormats);

        String parameter = value.substring(value.indexOf(" "));
        if (parameter.contains(MODE_SET)) {
            String modeSetStr = parameter.substring(parameter.indexOf(MODE_SET));
            modeSetStr = modeSetStr.substring(modeSetStr.indexOf("=") + 1);
            if (modeSetStr.contains(";")) {
                modeSetStr = modeSetStr.substring(0, modeSetStr.indexOf(';')).trim();
            }

            if (modeSetStr.contains(",")) {
                String[] modeSetStrList = modeSetStr.split(",");
                for (String curModeSetStr : modeSetStrList) {
                    modeSetList.add(Integer.parseInt(curModeSetStr.trim()));
                }
            } else if (modeSetStr.contains("-")) {
                String[] modeSetStrList = modeSetStr.split("-");
                if (modeSetStrList.length == 1) {
                    int minModeSet = Integer.parseInt(modeSetStrList[0].trim());
                    modeSetList.add(minModeSet);
                } else if (modeSetStrList.length == 2) {
                    int minModeSet = Integer.parseInt(modeSetStrList[0].trim());
                    int maxModeSet = Integer.parseInt(modeSetStrList[1].trim());
                    for (int i = minModeSet; i <= maxModeSet; i++) {
                        modeSetList.add(i);
                    }
                }
            } else {
                modeSetList.add(Integer.parseInt(modeSetStr));
            }
        }

        if (parameter.contains(OCTET_ALIGN)) {
            String octetAlignStr = parameter.substring(parameter.indexOf(OCTET_ALIGN));
            octetAlignStr = octetAlignStr.substring(octetAlignStr.indexOf("=") + 1);
            if (octetAlignStr.contains(";")) {
                isOaMode = Integer.parseInt(octetAlignStr.substring(0, octetAlignStr.indexOf(';')).trim()) == 1;
            } else {
                isOaMode = Integer.parseInt(octetAlignStr.substring(0, 1)) == 1;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public List<Integer> getModeSetList() {
        return modeSetList;
    }

    public int getModeSetMax() {
        if (!modeSetList.isEmpty()) {
            return Collections.max(modeSetList);
        } else {
            return -1;
        }
    }

    public int getModeSetMin() {
        if (!modeSetList.isEmpty()) {
            return Collections.min(modeSetList);
        } else {
            return -1;
        }
    }

    public void addModeSet(int modeSet) {
        this.modeSetList.add(modeSet);
    }

    public boolean isOaMode() {
        return isOaMode;
    }

    public void setOaMode(boolean oaMode) {
        isOaMode = oaMode;
    }
}
