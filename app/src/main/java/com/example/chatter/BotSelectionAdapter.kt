import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.BotSelectedInterface
import com.example.chatter.R
import kotlinx.android.synthetic.main.nations_item_view.view.*

class BotSelectionAdapter(
    val context: Context,
    var bots: ArrayList<String>,
    var botImages: ArrayList<String>,
    var botSelectedInterface: BotSelectedInterface
) :
    RecyclerView.Adapter<BotSelectionAdapter.BotSelectionViewHolder>() {
    private var selectedPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotSelectionViewHolder {
        return BotSelectionViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.nations_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BotSelectionViewHolder, position: Int) {
        val botTitle = bots[position]
        val botImg = botImages[position]
        holder.itemView.nations_item_layout.setOnClickListener {
            selectBotItem(position)
        }
        holder.bind(botTitle, botImg, position)
    }

    private fun selectBotItem(position: Int) {
        selectedPos = position
        botSelectedInterface.onBotSelected(bots[selectedPos], botImages[selectedPos])
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = bots.size

    inner class BotSelectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var imageView: ImageView? = null

        init {
            imageView = itemView.flagImg
        }

        fun bind(botTitle: String, botImg: String, position: Int) {
            itemView.country.text = botTitle
            imageView?.let {
                Glide.with(context)
                    .load(botImg)
                    .into(it)
            }
            if (selectedPos == position) {
                itemView.language_selector_green_check.visibility = View.VISIBLE
                itemView.nations_item_layout.setBackgroundResource(R.drawable.nation_item_view_background_enabled)
            } else {
                itemView.language_selector_green_check.visibility = View.GONE
                itemView.nations_item_layout.setBackgroundResource(R.drawable.nation_item_view_background)
            }
        }
    }
}