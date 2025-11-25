package nuist.cn.mymoment.ui.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nuist.cn.mymoment.R
import nuist.cn.mymoment.model.Diary

class DiaryAdapter(
    private val listener: DiaryActionListener
) : ListAdapter<Diary, DiaryAdapter.DiaryViewHolder>(DiffCallback) {

    private var largeFont = false

    fun setLargeFont(enabled: Boolean) {
        largeFont = enabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position), largeFont, listener)
    }

    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.diaryTitle)
        private val content: TextView = itemView.findViewById(R.id.diaryContent)
        private val location: TextView = itemView.findViewById(R.id.diaryLocation)
        private val meta: TextView = itemView.findViewById(R.id.diaryMeta)
        private val menuAnchor: View = itemView.findViewById(R.id.diaryMenu)

        fun bind(diary: Diary, largeFont: Boolean, listener: DiaryActionListener) {
            title.text = diary.title
            content.text = diary.content
            location.text = if (diary.locationName.isNotBlank()) diary.locationName else itemView.context.getString(
                R.string.diary_location_unknown
            )
            meta.text = buildString {
                if (diary.weather.isNotBlank()) append(diary.weather)
                if (diary.temperature.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(diary.temperature)
                }
            }

            val titleSize = if (largeFont) 22f else 18f
            val contentSize = if (largeFont) 18f else 14f
            title.textSize = titleSize
            content.textSize = contentSize

            itemView.setOnClickListener { listener.onEditDiary(diary) }
            menuAnchor.setOnClickListener {
                PopupMenu(itemView.context, menuAnchor).apply {
                    inflate(R.menu.diary_item_menu)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_edit -> listener.onEditDiary(diary)
                            R.id.action_delete -> listener.onDeleteDiary(diary)
                        }
                        true
                    }
                }.show()
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Diary>() {
        override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean = oldItem == newItem
    }
}

interface DiaryActionListener {
    fun onEditDiary(diary: Diary)
    fun onDeleteDiary(diary: Diary)
}

