package cn.himavat.bpr.wework;

import java.util.Date;

public class MsgUtil {
    public static Date handleMsgValue(String msgValue){
        int val = Integer.parseInt(msgValue);
        if(val<0){

        }else if(val == 0){

        }else if(val > 0 && val > 20000){

        }else if(val > 30000 && val < 40000){

        }
        return null;
    }

    /**
     * 由测量值换算成精确的电流数据
     * @param measuredVal
     * @return
     * 电压单位毫伏，值是double voltage = measuredVal * (3300 / 4096);
     * 电流单位毫安，值是double current = voltage / 0.8;
     */
    public static double calculatePreciseCurrent(int measuredVal){
        return measuredVal * (4125 / 4096);
    }
}
