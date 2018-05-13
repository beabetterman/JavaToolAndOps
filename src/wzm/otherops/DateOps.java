package wzm.otherops;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Date Process:
 * 		https://blog.csdn.net/jediael_lu/article/details/43852043
 * 考虑线程安全性问题：
 * 		JDK8的应用，可以使用Instant代替Date，LocalDateTime代替Calendar，DateTimeFormatter代替SimpleDateFormat，
 * 		官方给出的解释：simple beautiful strong immutable thread-safe。
 */

public class DateOps {
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
    
    
    public static void main(String[] argv) {
        System.out.println("Time is :" + simpleDateFormat.format(System.currentTimeMillis()));
        
        dateOps();
    }
    
    /**
     * 
     */
    public static void format() {
        // SimpleDateFormat 定义日期的格式，用于交互。
        // 创建 SimpleDateFormat 对象时必须指定转换格式。转换格式区分大小写，yyyy 代表年份，MM 代表月份，dd 代表日期，HH 代表 24 进制的小时，hh 代表 12 进制的小时，mm 代表分钟，ss 代表秒。
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        
        // Date/Time to String.
        long nowTimeByLong = System.currentTimeMillis();
        System.out.println(simpleDateFormat.format(nowTimeByLong));
        System.out.println(simpleDateFormat.format(new Date()));
        
        // String to Date/Time
        String oneDay = "2017-11-19 13:01:01";
        try {
            simpleDateFormat.parse(oneDay);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    
    /**
     * 获取日期中的信息，年月日，星期，月内的天数，等等
     */
    public static void getDateInfo() {
        // 可以设置时区TimeZone和语言Local.XXX，也可以只输入其中一个参数
        Calendar calendar = Calendar.getInstance();
        // 设置对应的值。
        // calendar.set(field, value);
        // 获取对应的值。
        int year = calendar.get(Calendar.YEAR); // 当前年份
        int month = calendar.get(Calendar.MONTH) + 1; // 当前月，index从0开始。注意加 1
        int day = calendar.get(Calendar.DATE); //当前日 index 从1开始。
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK); // 第一天是周日，index 从 0 开始。
        Date date = calendar.getTime(); //直接取得一个 Date 类型的日期
        // 其他的Field值： DAY_OF_MONTH, DAY_OF_WEEK, HOUR(12小时制), HOUR_OF_DAY(24小时制),MINUTE,SECOND
        
    }
    
    /**
     * 日期的设定、运算（加减）操作，比较，以及日期相关的统计信息。
     */
    public static void dateOps() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2017); // 可以设置各个纬度的值。Year, month, day等等。
        calendar.setTime(new Date()); // 通过Date() 直接设置。
        
        calendar.add(Calendar.DAY_OF_YEAR, 1); // 各个纬度都可以进行操作。
        calendar.add(Calendar.DAY_OF_YEAR, -1); // 各个纬度都可以进行操作。提前模式，通过负数来计算。
        
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 各个纬度的最大值，最小值。以及日期相关的统计信息。
        calendar.getFirstDayOfWeek();
        calendar.getWeeksInWeekYear(); 
        
        // 比较： Date()  和 Calendar都有比较函数。选择合适的使用。
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        boolean early = calendar.before(now); // 参数是Calendar类型，因为最终是通过Calendar的比较函数进行比较的。。
        boolean late = calendar.after(now); // 
        if (early) {
            System.out.println(simpleDateFormat.format(calendar.getTime()) +" is early than now.");
        } else {
            System.out.println(simpleDateFormat.format(calendar.getTime()) +" is late than now.");
        }
    }
    
    
    /**
     * 获取日期对应的毫秒数。
     * @return
     */
    public static long getTimeByLong() {
        // Method 1
        Date date = new Date();
        date = Calendar.getInstance().getTime();
        long time = date.getTime();
        // Method 2
        time = System.currentTimeMillis();
        return time;
    }
    
    
}
