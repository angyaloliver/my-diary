package com.example.mdiary.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mdiary.R
import com.example.mdiary.ScrollingActivity
import com.example.mdiary.data.AppDatabase
import com.example.mdiary.data.DiaryItem
import kotlinx.android.synthetic.main.diary_item.view.*
import java.time.format.DateTimeFormatter

class DiaryAdapter : RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    var diaryItems = mutableListOf<DiaryItem>()
    val context : Context

    constructor(context: Context, diaryItemList: List<DiaryItem>) :super() {
        this.context = context
        diaryItems.addAll(diaryItemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val diaryView = LayoutInflater.from(context).inflate(
            R.layout.diary_item, parent, false
        )
        return ViewHolder(diaryView)
    }

    override fun getItemCount(): Int {
        return diaryItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diaryItem = diaryItems[position]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
            var formattedDate = diaryItem.createDate?.format(formatter)
            holder.tvDate.text = formattedDate
        } else {
            holder.tvDate.text = diaryItem.createDate
        }
        holder.tvDate.leftDrawable(R.drawable.calendar_icon,R.dimen.icon_size)

        holder.tvIsPersonal.text= if(diaryItem.isPersonal) "Personal" else "Work related"
        holder.tvIsPersonal.leftDrawable(R.drawable.work_icon,R.dimen.icon_size)

        holder.tvTitle.text = diaryItem.title

        holder.tvPlace.text=diaryItem.createPlace
        holder.tvPlace.leftDrawable(R.drawable.home_icon,R.dimen.icon_size)

        holder.tvDescription.text=diaryItem.description
        holder.tvDescription.leftDrawable(R.drawable.pencil_icon,R.dimen.icon_size)

        if(diaryItem.longitude != null && diaryItem.latitude != null){
            holder.tvLocation.text = ("" + diaryItem.longitude + ":" + diaryItem.latitude)

        }
        holder.tvLocation.leftDrawable(R.drawable.location_icon,R.dimen.icon_size)

        if(!diaryItem.photoAbsolutePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(diaryItem.photoAbsolutePath)
            holder.ivPhoto.setImageBitmap(bitmap)
            holder.ivPhoto.visibility = View.VISIBLE
        }

        holder.btnDelete.setOnClickListener {
            deleteDiaryItem(holder.adapterPosition)
        }
    }

    fun TextView.leftDrawable(@DrawableRes id: Int = 0, @DimenRes sizeRes: Int) {
        val drawable = ContextCompat.getDrawable(context, id)
        val size = resources.getDimensionPixelSize(sizeRes)
        drawable?.setBounds(0, 0, size, size)
        this.setCompoundDrawables(drawable, null, null, null)
    }

    fun deleteDiaryItem(position: Int) {
        Thread {
            AppDatabase.getInstance(context).diaryItemDAO().deleteDiaryItem(diaryItems[position])

            (context as ScrollingActivity).runOnUiThread {
                diaryItems.removeAt(position)
                notifyItemRemoved(position)
            }
        }.start()
    }

    fun addDiaryItem(diaryItem: DiaryItem) {
        diaryItems.add(diaryItem)
        notifyItemInserted(diaryItems.lastIndex)
    }

    fun deleteAll(){
        Thread{
            AppDatabase.getInstance(context).diaryItemDAO().deleteAll()
            var size = diaryItems.size
            (context as ScrollingActivity).runOnUiThread {
                diaryItems.clear()
                notifyItemRangeRemoved(0, size)
            }
        }.start()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate = itemView.tvDate
        val tvPlace = itemView.tvPlace
        val tvTitle = itemView.tvTitle
        val tvDescription = itemView.tvDescription
        val tvIsPersonal = itemView.tvIsPersonal
        val btnDelete = itemView.btnDelete
        val tvLocation = itemView.tvLocation
        val ivPhoto = itemView.ivAttachedPhoto
    }
}