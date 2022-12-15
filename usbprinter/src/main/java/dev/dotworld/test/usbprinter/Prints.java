package dev.dotworld.test.usbprinter;

import static dev.dotworld.test.usbprinter.DateAndRandomNoKt.getDateAnDTime;
import static dev.dotworld.test.usbprinter.DateAndRandomNoKt.getRandom;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lvrenyang.io.Pos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class Prints {

    static String TAG = "Prints";

    public static int PrintTicket(
            Context ctx,
            Pos pos,
            int nPrintWidth,
            boolean bCutter,
            boolean bDrawer,
            boolean bBeeper,
            int nCount,
            int nPrintContent,
            int nCompressMethod,
            String data,
            Bitmap image
    ) {
        int bPrintResult = -8;
        Bitmap img = image;
        byte[] status = new byte[1];
        Log.d(TAG, "PrintTicket: " + pos.POS_RTQueryStatus(status, 3, 1000, 2));
        if (pos.POS_RTQueryStatus(status, 3, 1000, 2)) {

            if ((status[0] & 0x08) == 0x08)   //Determine whether the cutter is abnormal
                return bPrintResult = -2;

            if ((status[0] & 0x40) == 0x40)   //Determine whether the print head is within the normal range
                return bPrintResult = -3;

            if (pos.POS_RTQueryStatus(status, 2, 1000, 2)) {

                if ((status[0] & 0x04) == 0x04)    //Determine whether the cover is normal
                    return bPrintResult = -6;
                if ((status[0] & 0x20) == 0x20)    //Determine if there is no paper
                    return bPrintResult = -5;
                else {
                    for (int i = 0; i < nCount; i++) {

                        if (!pos.GetIO().IsOpened())
                            break;

                        if (nPrintContent == 1) {

                            pos.POS_Reset();
                            pos.POS_FeedLine();
                            if (img != null) {
                                pos.POS_PrintPicture(img, 100, 1, 0);
                                pos.POS_FeedLine();
                                pos.POS_FeedLine();
                                pos.POS_FeedLine();
                            } else {
                                pos.POS_S_TextOut("no image", 50, 10, 0, 4, 0x08);
                            }
                            pos.POS_FeedLine();
                            pos.POS_FeedLine();
                            pos.POS_FeedLine();

                        } else if (nPrintContent >= 2) {
                            if (data == null && image != null) {
                                pos.POS_Reset();
                                pos.POS_FeedLine();
                                if (img != null) {
                                    pos.POS_PrintPicture(img, 100, 1, 0);
                                    pos.POS_FeedLine();
                                    pos.POS_FeedLine();
                                    pos.POS_FeedLine();
                                } else {
                                    pos.POS_S_TextOut("no image", 50, 10, 0, 4, 0x08);
                                }
                            }
                            pos.POS_FeedLine();
                            pos.POS_FeedLine();
                            pos.POS_FeedLine();
                        }
                    }
                }

                if (bBeeper)
                    pos.POS_Beep(1, 5);
                if (bCutter && nCount == 1)
                    pos.POS_FullCutPaper();
                if (bDrawer)
                    pos.POS_KickDrawer(0, 100);


                if (nCount == 1) {
                    try {
                        Thread.currentThread();
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            return bPrintResult = -8;           //Query failed
        }
        return bPrintResult = 0;
    }

    public static String ResultCodeToString(int code) {
        switch (code) {
            case 3:
                return "There is an uncollected receipt at the paper outlet, please take it out in time";
            case 2:
                return "The paper will run out and there is an uncollected receipt at the paper outlet," +
                        " please pay attention to replace the paper roll and take away the receipt in time";
            case 1:
                return "The paper is almost out, please pay attention to replace the paper roll";
            case 0:
                return " ";
            case -1:
                return "The receipt is not printed, please check for paper jams";
            case -2:
                return "The cutter is abnormal, please remove it manually";
            case -3:
                return "\n" +
                        "The print head is too hot, please wait for the printer to cool down";
            case -4:
                return "The printer is offline";
            case -5:
                return "Printer out of paper";
            case -6:
                return "Cover open";
            case -7:
                return "Real-time status query failed";
            case -8:
                return "Failed to query the status, please check whether the communication port is connected normally";
            case -9:
                return "Out of paper during printing, please check the document integrity";
            case -10:
                return "The top cover is opened during printing, please print again";
            case -11:
                return "The connection is interrupted, please confirm whether the printer is connected";
            case -12:
                return "Please remove the printed receipt before printing!";
            case -13:
            default:
                return "unknown mistake";
        }
    }

    /**
     * Read pictures from Assets
     */
    public static Bitmap getImageFromAssetsFile(Context ctx, String fileName) {
        Bitmap image = null;
        AssetManager am = ctx.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


}
