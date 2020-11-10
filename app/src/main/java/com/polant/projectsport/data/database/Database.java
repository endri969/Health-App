package com.polant.projectsport.data.database;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.polant.projectsport.constants.Constants;
import com.polant.projectsport.R;
import com.polant.projectsport.theme.ThemeSettings;
import com.polant.projectsport.model.Article;
import com.polant.projectsport.model.SpecificFood;
import com.polant.projectsport.model.StatisticsDay;
import com.polant.projectsport.model.UserParametersInfo;
import com.polant.projectsport.fragment.chart.ChartStatisticsFragment;
import com.polant.projectsport.preferences.PreferencesNewActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


public class Database {

    Context context;

    SportOpenHelper openHelper;
    SQLiteDatabase sqLiteDatabase;

    public Database(Context _context) {
        context = _context;
    }

    public void open(){
        openHelper = new SportOpenHelper(context);
        sqLiteDatabase = openHelper.getWritableDatabase();
    }

    public void close(){
        if (openHelper != null) openHelper.close();
    }

    public SQLiteDatabase getSqLiteDatabase(){
        return sqLiteDatabase;
    }

    //Получение необходимой статистики о потреблении пищи пользователем.
    public ArrayList<StatisticsDay> getStatistics(int interval){
        ArrayList<StatisticsDay> list;

        if (interval == ChartStatisticsFragment.STATISTICS_MONTH){
            list = getListMonthStatisticsItems();
        }
        else{
            list = getListWeekStatisticsItems();
        }
        return list;
    }


    private ArrayList<StatisticsDay> getListWeekStatisticsItems() {
        ArrayList<StatisticsDay> list = new ArrayList<>();

        Cursor data = getRawWeekStatisticsItems();
        if (data != null){
            if (data.moveToLast()){
                int deltaIndex = data.getColumnIndex(DELTA);
                int dayIndex = data.getColumnIndex(DAY);
                int monthIndex = data.getColumnIndex(MONTH);
                int yearIndex = data.getColumnIndex(YEAR);

                int tempDay = data.getInt(dayIndex);
                int currentDay = tempDay;
                int month =  data.getInt(monthIndex);
                int year = data.getInt(yearIndex);
                int sumDayDelta = 0;

                int i = 0;
                int count = ChartStatisticsFragment.STATISTICS_WEEK;
                do {
                    tempDay = data.getInt(dayIndex);
                    if (tempDay != currentDay){
                        list.add(new StatisticsDay(currentDay, month, year, sumDayDelta));
                        sumDayDelta = 0;
                        currentDay = tempDay;
                        i++;
                    }
                    sumDayDelta += data.getInt(deltaIndex);
                    month = data.getInt(monthIndex);
                    year = data.getInt(yearIndex);
                }while (data.moveToPrevious() && i <= count);

                list.add(new StatisticsDay(tempDay, month, year, sumDayDelta));
                data.close();
            }
            else
                data.close();
        }
        Collections.reverse(list);
        return list;
    }


    private ArrayList<StatisticsDay> getListMonthStatisticsItems(){
        ArrayList<StatisticsDay> list = new ArrayList<>();

        Cursor data = getRawMonthStatisticsItems();
        if (data != null){
            if (data.moveToFirst()){
                int deltaIndex = data.getColumnIndex(DELTA);
                int dayIndex = data.getColumnIndex(DAY);
                int monthIndex = data.getColumnIndex(MONTH);
                int yearIndex = data.getColumnIndex(YEAR);

                int tempDay = data.getInt(dayIndex);
                int currentDay = tempDay;
                int month = 0;
                int year = 0;

                int sumDayDelta = 0;
                do {
                    tempDay = data.getInt(dayIndex);
                    if (tempDay != currentDay){
                        list.add(new StatisticsDay(currentDay, month, year, sumDayDelta));
                        sumDayDelta = 0;
                        currentDay = tempDay;
                    }
                    sumDayDelta += data.getInt(deltaIndex);
                    month = data.getInt(monthIndex);
                    year = data.getInt(yearIndex);

                }while (data.moveToNext());
                list.add(new StatisticsDay(tempDay, month, year, sumDayDelta));
                data.close();
            }
            else
                data.close();
        }
        return list;
    }


    private Cursor getRawWeekStatisticsItems() {
        String[] projection = new String[] {
                DELTA, DAY, MONTH, YEAR
        };
        return sqLiteDatabase.query(TABLE_STATISTICS, projection, null, null, null, null, null);
    }



    private Cursor getRawMonthStatisticsItems(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        //int monthFrom = month;
        int yearFrom = year;
        if (month - 1 < 0){
            //monthFrom = 11;
            month = 11;
            yearFrom = --year;
        }
        else{
            month--;
        }
        String[] projection = new String[] {
                DELTA, DAY, MONTH, YEAR
        };
        String where = "(" + DAY + " >? AND " + MONTH + " >? AND " + YEAR + " >? AND " + YEAR + " <? ) OR ( " +
                DAY + " >? AND " + MONTH + " >? AND " + YEAR + " =? ) OR ( " +
                MONTH + ">? AND " + YEAR + "=? )";

         String[] whereArgs = new String[] {
                 String.valueOf(day - 1), String.valueOf(month - 1), String.valueOf(yearFrom - 1), String.valueOf(yearFrom + 1),

                 String.valueOf(day - 1), String.valueOf(month - 1), String.valueOf(year),

                 String.valueOf(month), String.valueOf(year)
         };
        return sqLiteDatabase.query(TABLE_STATISTICS, projection, where, whereArgs, null, null, null);
    }



    public void addArticle(Article article){
        ContentValues cv = new ContentValues();

        cv.put(ARTICLE_CATEGORY, article.getCategory());
        cv.put(ARTICLE_TITLE, article.getTitle());
        cv.put(ARTICLE_TEXT, article.getText());
        cv.put(ARTICLE_DATE, article.getDate());

        sqLiteDatabase.insert(TABLE_ARTICLE, null, cv);
    }


    public Cursor getArticles(String[] projection, String category){
        String where = ARTICLE_CATEGORY + "=?";
        String[] whereArgs = new String[] { category };
        return sqLiteDatabase.query(TABLE_ARTICLE, projection, where, whereArgs, null, null, null);
    }


    public Article getArticle(int id){
        String where = ID_ARTICLE + "=" + id;
        Cursor c = sqLiteDatabase.query(TABLE_ARTICLE, null, where, null, null, null, null);

        if (c != null){
            if (c.moveToFirst()) {
                Article result = new Article();
                result.setCategory(c.getString(c.getColumnIndex(ARTICLE_CATEGORY)))
                        .setTitle(c.getString(c.getColumnIndex(ARTICLE_TITLE)))
                        .setText(c.getString(c.getColumnIndex(ARTICLE_TEXT)))
                        .setDate(c.getString(c.getColumnIndex(ARTICLE_DATE)));

                c.close();
                return result;
            }
            c.close();
        }
        return null;
    }


    public Cursor getFoodData(){
        String[] projection = new String[] {ID_FOOD + " AS " + "_id", FOOD_CATEGORY};
        return sqLiteDatabase.query(TABLE_FOOD, projection, null, null, null, null, null);
    }


    public Cursor getSpecificFoodData(String category){
        String query = "SELECT " + ID_SPECIFIC_FOOD + ", " + FOOD_NAME + ", " +
                FOOD_CATEGORY + ", " + CAL_COUNT +
                " FROM " + TABLE_FOOD + ", " + TABLE_SPECIFIC_FOOD + " " +
                "WHERE " + TABLE_FOOD + "." + ID_FOOD + "="
                         + TABLE_SPECIFIC_FOOD + "." + ID_FOOD +
                " AND " + FOOD_CATEGORY + "='" + category + "';";
        return sqLiteDatabase.rawQuery(query, null);
    }

    public void deleteStatisticsRecord(int id){
        String where = ID_STATISTICS + "=" + String.valueOf(id);
        sqLiteDatabase.delete(TABLE_STATISTICS, where, null);
    }

    public Cursor getTodayFoodStatistics(){

        Calendar calendar = Calendar.getInstance();

        String query = "SELECT " +
                    ID_STATISTICS + " AS _id, " +
                    //TABLE_SPECIFIC_FOOD + "." + ID_SPECIFIC_FOOD + ", " +
                    FOOD_CATEGORY + ", " +
                    FOOD_NAME + ", " +
                    CAL_COUNT + ", " +
                    DELTA + ", " +
                    DAY + ", " +
                    MONTH + ", " +
                    YEAR +
                " FROM " + TABLE_FOOD + ", " + TABLE_SPECIFIC_FOOD + ", " + TABLE_STATISTICS +
                " WHERE " + TABLE_FOOD + "." + ID_FOOD + "="
                         + TABLE_SPECIFIC_FOOD + "." + ID_FOOD +
                    " AND " + TABLE_STATISTICS + "." + ID_SPECIFIC_FOOD + "="
                         + TABLE_SPECIFIC_FOOD + "." + ID_SPECIFIC_FOOD +
                    " AND " + DAY + "=" + calendar.get(Calendar.DATE) +
                    " AND " + MONTH + "=" + calendar.get(Calendar.MONTH) +
                    " AND " + YEAR + "=" + calendar.get(Calendar.YEAR) + ";";
        return sqLiteDatabase.rawQuery(query, null);
    }


    public void addSpecificFood(SpecificFood food, float delta){
        ContentValues cv = new ContentValues();


        Calendar calendar = Calendar.getInstance();

        cv.put(ID_SPECIFIC_FOOD, food.getIdSpecificFood());
        cv.put(DELTA, delta);
        cv.put(DAY, calendar.get(Calendar.DATE));
        cv.put(MONTH, calendar.get(Calendar.MONTH));
        cv.put(YEAR, calendar.get(Calendar.YEAR));

        sqLiteDatabase.insert(TABLE_STATISTICS, null, cv);
    }


    //Рассчитано на одного пользователя.
    public UserParametersInfo getUserParametersInfo(){
        String[] projection = new String[] {
                USER_NAME, USER_AGE, USER_WEIGHT, USER_HEIGHT, USER_SEX
        };

        Cursor c = sqLiteDatabase.query(TABLE_USER, projection, null, null, null, null, null);
        c.moveToFirst();

        float weight = c.getFloat(c.getColumnIndex(USER_WEIGHT));
        float height = c.getFloat(c.getColumnIndex(USER_HEIGHT));
        String sex = c.getString(c.getColumnIndex(USER_SEX));
        String name = c.getString(c.getColumnIndex(USER_NAME));
        int age = c.getInt(c.getColumnIndex(USER_AGE));

        c.close();

        return new UserParametersInfo(name, age, weight, height, sex);
    }

    public void updateUserParametersInfo(UserParametersInfo user){
        ContentValues cv = new ContentValues();
        cv.put(USER_WEIGHT, user.getWeight());
        cv.put(USER_HEIGHT, user.getHeight());
        cv.put(USER_SEX, user.getSex());
        cv.put(USER_AGE, user.getAge());
        cv.put(USER_NAME, user.getName());

        String where = USER_NAME + "=?";
        String[] whereArgs = new String[] { getUserParametersInfo().getName() };

        sqLiteDatabase.update(TABLE_USER, cv, where, whereArgs);
    }


    public void updateUserParametersInfo(SharedPreferences sp) {
        UserParametersInfo user;


        int age = Integer.valueOf(sp.getString(PreferencesNewActivity.PREF_USER_AGE, "20"));
        float weight = Float.valueOf(sp.getString(PreferencesNewActivity.PREF_USER_WEIGHT, "75"));
        float height = Float.valueOf(sp.getString(PreferencesNewActivity.PREF_USER_HEIGHT, "185"));
        if (age < Constants.MIN_AGE_VALUE
                || weight < Constants.MIN_WEIGHT_VALUE
                || height < Constants.MIN_HEIGHT_VALUE) {
            Toast.makeText(context, context.getString(R.string.toastMistakeMinValuesUserPrefInfo),
                    Toast.LENGTH_LONG)
                    .show();


            user = getUserParametersInfo();
            ThemeSettings.setUserParametersInfo(user, sp);
        }
        else {
            user = new UserParametersInfo();
            user.setSex(sp.getString(PreferencesNewActivity.PREF_USER_SEX, context.getString(R.string.text_your_sex_M)));
            user.setName(sp.getString(PreferencesNewActivity.PREF_USER_NAME, "Antony"));
            user.setAge(Integer.valueOf(sp.getString(PreferencesNewActivity.PREF_USER_AGE, "20")));
            user.setWeight(Float.valueOf(sp.getString(PreferencesNewActivity.PREF_USER_WEIGHT, "75")));
            user.setHeight(Float.valueOf(sp.getString(PreferencesNewActivity.PREF_USER_HEIGHT, "185")));
            updateUserParametersInfo(user);
        }
    }


    public static int getDatabaseVersion() {
        return SportOpenHelper.DATABASE_VERSION;
    }

    //----------------------------------------------------------//


    public static final String TABLE_USER = "USER_TABLE";
    public static final String TABLE_STATISTICS = "STATISTICS_TABLE";
    public static final String TABLE_SPECIFIC_FOOD = "SPECIFIC_FOOD_TABLE";
    public static final String TABLE_FOOD = "FOOD_TABLE";
    public static final String TABLE_ARTICLE  = "TABLE_ARTICLE";



    //ARTICLE
    public static final String ID_ARTICLE = "_id";
    public static final String ARTICLE_CATEGORY = "ARTICLE_CATEGORY";
    public static final String ARTICLE_TITLE = "ARTICLE_TITLE";
    public static final String ARTICLE_TEXT = "ARTICLE_TEXT";
    public static final String ARTICLE_DATE = "ARTICLE_DATE";

    //USER
    public static final String ID_USER = "ID_USER";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_WEIGHT = "USER_WEIGHT";
    public static final String USER_HEIGHT = "USER_HEIGHT";
    public static final String USER_SEX = "USER_SEX";
    public static final String USER_AGE = "USER_AGE";

    //FOOD
    public static final String ID_FOOD = "food_id";
    public static final String FOOD_CATEGORY = "FOOD_CATEGORY";

    //SPECIFIC_FOOD
    public static final String ID_SPECIFIC_FOOD = "_id";
    public static final String FOOD_NAME = "FOOD_NAME";
    //+ в таблице есть ID_FOOD как внешний ключ.
    public static final String CAL_COUNT = "CAL_COUNT";

    //STATISTICS
    public static final String ID_STATISTICS = "ID_STATISTICS";
    ////+ в таблице есть ID_SPECIFIC_FOOD как внешний ключ.
    public static final String DELTA = "DELTA";//или вместо разницы ("DELTA") хранить вес пищи?
    public static final String DAY = "DAY";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";

//----------------------------------------------------------//


    private static class SportOpenHelper extends SQLiteOpenHelper {

        private static final String LOG = SportOpenHelper.class.getName();

        private static final int DATABASE_VERSION = 28;

        private static final String DATABASE_NAME = "sport.db";


        private static final String CREATE_TABLE_USER = "Create table " + TABLE_USER + " (" +
                ID_USER + " integer primary key autoincrement, " +
                USER_NAME + " TEXT, " +
                USER_AGE + " INTEGER, " +
                USER_HEIGHT + " FLOAT, " +
                USER_WEIGHT + " FLOAT, " +
                USER_SEX + " TEXT);";

        private static final String CREATE_TABLE_FOOD =
                "Create table " + TABLE_FOOD + " (" +
                        ID_FOOD + " integer primary key autoincrement, " +
                        FOOD_CATEGORY + " TEXT);";

        private static final String CREATE_TABLE_SPECIFIC_FOOD =
                "Create table " + TABLE_SPECIFIC_FOOD + " (" +
                        ID_SPECIFIC_FOOD + " integer primary key autoincrement, " +
                        FOOD_NAME + " TEXT, " +
                        ID_FOOD + " INTEGER, " +
                        CAL_COUNT + " INTEGER);";

        private static final String CREATE_TABLE_STATISTICS =
                "Create table " + TABLE_STATISTICS + " (" +
                        ID_STATISTICS + " integer primary key autoincrement, " +
                        //ID_USER + " INTEGER, " +
                        ID_SPECIFIC_FOOD + " INTEGER, " +
                        DELTA + " FLOAT, " + //или вместо разницы ("DELTA") хранить вес пищи?
                        DAY + " INTEGER, " +
                        MONTH + " INTEGER, " +
                        YEAR + " INTEGER);";


        private static final String CREATE_TABLE_ARTICLE = "Create table " + TABLE_ARTICLE + " (" +
                ID_ARTICLE + " integer primary key autoincrement, " +
                ARTICLE_CATEGORY + " TEXT, " +
                ARTICLE_TITLE + " TEXT, " +
                ARTICLE_TEXT + " TEXT, " +
                ARTICLE_DATE + " TEXT);";



        public SportOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG, "----- Create database  -----");

            db.execSQL(CREATE_TABLE_USER);
            db.execSQL(CREATE_TABLE_FOOD);
            db.execSQL(CREATE_TABLE_SPECIFIC_FOOD);
            db.execSQL(CREATE_TABLE_STATISTICS);
            db.execSQL(CREATE_TABLE_ARTICLE);

            ContentValues cv = new ContentValues();
            UserParametersInfo user = new UserParametersInfo("Anton", 18, 75, 185, "М");
            cv.put(USER_NAME, user.getName());
            cv.put(USER_HEIGHT, user.getHeight());
            cv.put(USER_WEIGHT, user.getWeight());
            cv.put(USER_AGE, user.getAge());
            cv.put(USER_SEX, user.getSex());
            db.insert(TABLE_USER, null, cv);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG, "Update database from " + oldVersion + " to " + newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPECIFIC_FOOD + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLE + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATISTICS + ";");

            onCreate(db);
        }
    }


}
