package com.example.chapter6.chatlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chapter6.Key
import com.example.chapter6.R
import com.example.chapter6.chatdetail.ChatActivity
import com.example.chapter6.databinding.FragmentChatlistBinding
import com.example.chapter6.databinding.FragmentUserlistBinding
import com.example.chapter6.firebase.FirebaseConsts.USER_PATH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class ChatListFragment: Fragment(R.layout.fragment_chatlist) {

    private lateinit var binding: FragmentChatlistBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatlistBinding.bind(view)


        val chatListAdapter = ChatListAdapter { chatRoomItem ->
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, chatRoomItem.otherUserId)
            intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomItem.chatRoomId)

            startActivity(intent)
            setToken()
        }
        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }

        val currentUserId = Firebase.auth.currentUser?.uid ?: return
        val chatRoomsDB = Firebase.database.reference.child(Key.DB_CHAT_ROOM).child(currentUserId)

        chatRoomsDB.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomList = snapshot.children.map {
                    it.getValue(ChatRoomItem::class.java)
                }
                chatListAdapter.submitList(chatRoomList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    private fun setToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            if (it.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference(USER_PATH)
                    .child(FirebaseAuth.getInstance().uid!!)
                    .child("token")
                    .setValue(it)
                Log.e("token", it.toString())
            }
        }
    }
}