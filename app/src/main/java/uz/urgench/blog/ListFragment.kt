package uz.urgench.blog

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class ListFragment : Fragment() {
    private val textList: ArrayList<String> = arrayListOf()
    private val textNameList: ArrayList<String> = arrayListOf()
    private val userList: ArrayList<String> = arrayListOf()
    private val dateList: ArrayList<Timestamp> = arrayListOf()
    private lateinit var myItemRecyclerViewAdapter: MyItemRecyclerViewAdapter
    private lateinit var swipe_layout: SwipeRefreshLayout
    private lateinit var blogsList: RecyclerView
    private lateinit var addBlog: ImageButton
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blogsList = view.findViewById(R.id.list)
        addBlog = view.findViewById(R.id.addBlog)
        swipe_layout = view.findViewById(R.id.swipe_layout)
        putToList()
        addBlog.setOnClickListener {
            activity?.startActivity(Intent(activity, AddBlogActivity::class.java))
        }
        swipe_layout.setColorSchemeColors(resources.getColor(R.color.blue_and_purle))
        swipe_layout.setOnRefreshListener {
            putToList()
            swipe_layout.isRefreshing = false
        }
    }

    private fun putToList() {
        textList.clear()
        textNameList.clear()
        userList.clear()
        dateList.clear()
        val db = Firebase.firestore
        db.collection("Blog").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    textNameList.add(document.id)
                    textList.add(document.get("Text").toString())
                    userList.add(document.get("UserName").toString())
                    dateList.add(document.get("Date") as Timestamp)
                }
                blogsList.layoutManager = LinearLayoutManager(activity)
                val onUserClickListener: MyItemRecyclerViewAdapter.OnUserClickListener =
                    object : MyItemRecyclerViewAdapter.OnUserClickListener {
                        override fun onLongClick(textName: String) {
                        }

                        override fun onClick(textName: String) {
                        }
                    }
                myItemRecyclerViewAdapter = MyItemRecyclerViewAdapter(
                    textNameList,
                    textList,
                    userList,
                    dateList,
                    onUserClickListener
                )
                blogsList.adapter = myItemRecyclerViewAdapter
                val itemTouchHelper = ItemTouchHelper(gestures)
                itemTouchHelper.attachToRecyclerView(blogsList)
            }
    }

    val gestures = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float,
            dY: Float, actionState: Int, isCurrentlyActive: Boolean
        ) {
            RecyclerViewSwipeDecorator.Builder(
                activity!!,
                c, recyclerView, viewHolder, dX, dY,
                actionState, isCurrentlyActive
            )
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(activity!!, R.color.red))
                .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                .create()
                .decorate()
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            AlertDialog.Builder(activity)
                .setTitle("Вы уверены?")
                .setMessage("Вы уверены что хотите удалить запись ${textNameList[position]}?")
                .setPositiveButton("Да, удалить") { dial, id ->
                    Firebase.firestore.collection("Blog").document(textNameList[position]).delete()
                    Firebase.storage.reference.child("chatFiles/${textNameList[position]}")
                        .delete()
                    putToList()
                }.setNeutralButton("Отмена") { dial, id ->
                    myItemRecyclerViewAdapter.notifyDataSetChanged()
                }.setOnCancelListener { dial ->
                    myItemRecyclerViewAdapter.notifyDataSetChanged()
                }.show()
//            myItemRecyclerViewAdapter.removeItem(viewHolder as MyItemRecyclerViewAdapter.ViewHolder, viewHolder.absoluteAdapterPosition)
        }

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            if (userList[viewHolder.absoluteAdapterPosition] != Firebase.auth.currentUser?.email) return 0
            return super.getSwipeDirs(recyclerView, viewHolder)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_list, container, false)
    }
}