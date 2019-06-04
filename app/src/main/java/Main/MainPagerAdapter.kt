package Main

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup

class MainPagerAdapter(var viewList: MutableList<View>) : PagerAdapter() {

    private val mTitles = arrayOf("歌曲", "歌手", "歌曲列表")

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int {
       return viewList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList[position])
        return viewList[position]
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }
}