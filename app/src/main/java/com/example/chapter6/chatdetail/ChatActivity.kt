package com.example.chapter6.chatdetail

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chapter6.AccessToken
import com.example.chapter6.FirebaseMessaging
import com.example.chapter6.Key
import com.example.chapter6.R
import com.example.chapter6.api.NotificationAPI
import com.example.chapter6.databinding.ActivityChatBinding
import com.example.chapter6.firebase.FirebaseConsts.USER_PATH
import com.example.chapter6.model.Notification
import com.example.chapter6.model.NotificationData
import com.example.chapter6.userlist.UserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback
import kotlin.math.log

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var myUserId : String = ""
    private var myUserName : String = ""

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        myUserId =Firebase.auth.currentUser?.uid ?: ""
        val chatAdapter = ChatAdapter()
        linearLayoutManager = LinearLayoutManager(applicationContext)

        Firebase.database.reference.child(Key.DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""
            }
        Firebase.database.reference.child(Key.DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)

                chatAdapter.otherUserItem = otherUserItem
            }


        Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatItem =snapshot.getValue(ChatItem::class.java)
                chatItem ?: return

                chatItemList.add(chatItem)
                chatAdapter.submitList(chatItemList.toMutableList())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })

        binding.chatRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = chatAdapter
        }

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                linearLayoutManager.smoothScrollToPosition(binding.chatRecyclerView, null, chatAdapter.itemCount)
            }
        })

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext,"빈 메시지를 전송할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId,
            )

            Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
                "${Key.DB_CHAT_ROOM}/$myUserId/$otherUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOM}/$otherUserId/$myUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOM}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${Key.DB_CHAT_ROOM}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${Key.DB_CHAT_ROOM}/$otherUserId/$myUserId/otherUserName" to myUserName,
            )
            Firebase.database.reference.updateChildren(updates)
            sendNotification(message)

            binding.messageEditText.text.clear()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(applicationContext)
                .withPermission(Manifest.permission.POST_NOTIFICATIONS)
                .withListener(object : PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {}

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }
                }).check()
        }

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("test")



    }

    private fun sendNotification(message: String) {
        FirebaseDatabase.getInstance().getReference(USER_PATH)
            .child(FirebaseAuth.getInstance().uid!!)
            .child("token")
            .get().addOnSuccessListener {
                val token = it.getValue(String::class.java)

                val notification = Notification(
                    message = NotificationData(
                        token = token,
                        hashMapOf("title" to "Hello!", "body" to message)
                        )
                    )

                NotificationAPI.sendNotification().notification(notification).enqueue(object : retrofit2.Callback<Notification> {
                    override fun onResponse(p0: Call<Notification>, p1: Response<Notification>) {
                        Toast.makeText(this@ChatActivity, " Notification Send", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(p0: Call<Notification>, p1: Throwable) {
                        Toast.makeText(this@ChatActivity, "Error: ${p1.message}", Toast.LENGTH_SHORT).show()
                    }

                })
            }

    }

    companion object {
        const val EXTRA_CHAT_ROOM_ID = "CHAT_ROOM_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
    }



}