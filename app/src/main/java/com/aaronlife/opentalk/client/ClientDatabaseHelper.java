package com.aaronlife.opentalk.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientDatabaseHelper extends SQLiteOpenHelper
{
    public static class Contact
    {
        public String uid;
        public String name;
        public String datetime;

        public Contact()
        {
        }

        public Contact(String uid, String name, String datetime)
        {
            this.name = name;
            this.uid = uid;
            this.datetime = datetime;
        }
    }

    public static class ChatMessage implements Serializable
    {
        public final static int TYPE_MESSAGE = 0;
        public final static int TYPE_STICKER = 1;

        public int _id;
        public String fromUid;
        public String toUid;
        public int type;
        public String content;
        public String chatTime;

        public ChatMessage() {}

        public ChatMessage(String fromUid, String toUid, String content, String chatTime, int type)
        {
            this.fromUid = fromUid;
            this.toUid = toUid;
            this.type = type;
            this.content = content;
            this.chatTime = chatTime;
        }
    }

    private static final String DATABASE_NAME = "InstantTalkDB";
    private static final String TABLE_CONTACTS = "contacts";
    private static final String TABLE_INVITATIONS = "invitations";
    private static final String TABLE_CHATS = "chats";
    private static final int DATABASE_VERSION = 1;

    private ArrayList<Contact> contacts = new ArrayList<>();
    private ArrayList<Contact> invitations = new ArrayList<>();
    private CopyOnWriteArrayList<ChatMessage> chatMessages =
            new CopyOnWriteArrayList<>();

    private Context context;
    private static ClientDatabaseHelper mInstance = null;

    synchronized public static ClientDatabaseHelper getInstance(Context context)
    {
        if (mInstance == null)
        {
            mInstance =
                   new ClientDatabaseHelper(context.getApplicationContext(),
                                            DATABASE_NAME, null,
                                            DATABASE_VERSION);
            mInstance.init();
        }

        return mInstance;
    }

    private ClientDatabaseHelper(Context context,
                                String name,
                                SQLiteDatabase.CursorFactory factory,
                                int version)
    {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // 建立要存放資料的資料表格(相當於Excel的sheet)
        // 1. SQL語法不分大小寫
        // 2. 這裡大寫代表的是SQL標準語法, 小寫字是資料表/欄位的命名

        // 聯絡人table
        db.execSQL("CREATE TABLE " +
                   TABLE_CONTACTS +
                   "(uid TEXT PRIMARY KEY NOT NULL," +
                   "name TEXT NOT NULL," +
                   "reg_time TEXT)");

        // 好友邀請table
        db.execSQL("CREATE TABLE " +
                   TABLE_INVITATIONS +
                   "(uid TEXT PRIMARY KEY NOT NULL," +
                   "name TEXT NOT NULL," +
                   "invite_time TEXT)");

        // 聊天記錄table
        db.execSQL("CREATE TABLE " +
                   TABLE_CHATS +
                   "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                   "from_uid TEXT NOT NULL," +
                   "to_uid TEXT NOT NULL," +
                   "type INTEGER NOT NULL," +
                   "content TEXT NOT NULL," +
                   "chat_time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    private void init()
    {
        SQLiteDatabase db = getReadableDatabase();

        // 透過query來查詢資料
        Cursor c = db.query(TABLE_CONTACTS,           // 資料表名字
                   new String[]{"uid", "name", "reg_time"}, // 要取出的欄位資料
                   null,                              // 查詢條件式
                   null,                              // 查詢條件值字串陣列
                   null,                              // Group By字串語法
                   null,                              // Having字串法
                   null,                              // Order By字串語法(排序)
                   null);                             // Limit字串語法

        while(c.moveToNext())
        {
            Contact d = new Contact();
            d.uid = c.getString(c.getColumnIndex("uid")); // 取出名字欄位資料
            d.name = c.getString(c.getColumnIndex("name")); // 取出名字欄位資料
            d.datetime = c.getString(c.getColumnIndex("reg_time")); // 名字欄位資料
            contacts.add(d);
        }

        // 釋放資源
        c.close();

        // 透過query來查詢聊天記錄
        c = db.query(TABLE_CHATS,                     // 資料表名字
                     new String[]{"_id", "from_uid", "to_uid", "type", "content", "chat_time"},  // 要取出的欄位資料
                     null,                            // 查詢條件式
                     null,                            // 查詢條件值字串陣列
                     null,                            // Group By字串語法
                     null,                            // Having字串法
                     null,                            // Order By字串語法(排序)
                     null);                           // Limit字串語法

        while(c.moveToNext())
        {
            ChatMessage d = new ChatMessage();
            d._id = c.getInt(c.getColumnIndex("_id"));             // 取出名字欄位資料
            d.fromUid = c.getString(c.getColumnIndex("from_uid")); // 取出名字欄位資料
            d.toUid = c.getString(c.getColumnIndex("to_uid"));     // 取出名字欄位資料
            d.type = c.getInt(c.getColumnIndex("type"));           // 取出名字欄位資料
            d.content = c.getString(c.getColumnIndex("content"));  // 取出名字欄位資料
            d.chatTime = c.getString(c.getColumnIndex("chat_time"));// 取出名字欄位資料
            chatMessages.add(d);
        }

        c.close();

        // 透過query來查詢邀請
        c = db.query(TABLE_INVITATIONS,               // 資料表名字
                     new String[]{"uid", "name", "invite_time"},  // 要取出的欄位資料
                     null,                                 // 查詢條件式
                     null,                                 // 查詢條件值字串陣列
                     null,                                 // Group By字串語法
                     null,                                 // Having字串法
                     null,                                 // Order By字串語法(排序)
                     null);                                // Limit字串語法

        while(c.moveToNext())
        {
            Contact d = new Contact();
            d.uid = c.getString(c.getColumnIndex("uid"));    // 取出名字欄位資料
            d.name = c.getString(c.getColumnIndex("name"));    // 取出名字欄位資料
            d.datetime = c.getString(c.getColumnIndex("invite_time"));    // 取出名字欄位資料
            invitations.add(d);
        }

        c.close();
        db.close();
    }

    public ArrayList<Contact> queryAllContacts()
    {
        return contacts;
    }

    public void insertContact(Contact contact)
    {
        // 新增到資料庫
        SQLiteDatabase db = getWritableDatabase();

        // 定義要新增的資料
        ContentValues values = new ContentValues();
        values.put("uid", contact.uid);
        values.put("name", contact.name);
        values.put("reg_time", contact.datetime);

        // 新增一筆資料到資料表(Table)
        db.insert(TABLE_CONTACTS, null, values);

        // 寫入ArrayList
        contacts.add(contact);

        // 釋放SQLiteDatabase資源
        db.close();
    }

    //
    //
    //
    public ArrayList<Contact> queryAllInvitation()
    {
        return invitations;
    }

    public void insertInvitation(Contact contact)
    {
        // 新增到資料庫
        SQLiteDatabase db = getWritableDatabase();

        // 定義要新增的資料
        ContentValues values = new ContentValues();
        values.put("uid", contact.uid);
        values.put("name", contact.name);
        values.put("invite_time", contact.datetime);

        // 新增一筆資料到資料表(Table)
        db.insert(TABLE_INVITATIONS, null, values);

        // 寫入ArrayList
        invitations.add(contact);

        // 釋放SQLiteDatabase資源
        db.close();
    }

    public void deleteInvitation(String uid)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_INVITATIONS, "uid='" + uid + "'", null);
        db.close();

        for(Contact c : invitations)
        {
            if(c.uid.equals(uid))
            {
                invitations.remove(c);
                break;
            }
        }
    }

    public ArrayList<ChatMessage> queryChatMessage(String uid)
    {
        ArrayList<ChatMessage> result = new ArrayList<>();

        for(ChatMessage cm : chatMessages)
        {
            if(cm.fromUid.equals(uid) || cm.toUid.equals(uid))
            {
                result.add(cm);
            }
        }

        return result;
    }

    public void insertChatMessage(ChatMessage chatMessage)
    {
        // 新增到資料庫
        SQLiteDatabase db = getWritableDatabase();

        // 定義要新增的資料
        ContentValues values = new ContentValues();
        values.put("from_uid", chatMessage.fromUid);
        values.put("to_uid", chatMessage.toUid);
        values.put("type", chatMessage.type);
        values.put("content", chatMessage.content);
        values.put("chat_time", chatMessage.chatTime);

        // 新增一筆資料到資料表(Table)
        db.insert(TABLE_CHATS, null, values);

        // 寫入ArrayList
        chatMessages.add(chatMessage);

        // 釋放SQLiteDatabase資源
        db.close();
    }
}