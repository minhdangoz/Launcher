1. 定义RecyclerViewAdapter
<pre>
<code>
public class MyRecyclerViewAdapter extends BaseRecyclerViewAdapter<String> {
    public MyRecyclerViewAdapter(Context context, List<String> elementList, int layoutXmlId) {
        super(context, elementList, layoutXmlId);
    }

    @Override
    public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
        String element = mElementList.get(position);

    }
}
</code>
<pre>




1、坑一：adaper设置wrap_content不管用

版本要用23.2.1之后版本，因为之前版本有bug，例如RecyclerView的adapter，设置wrap_conent不管用

2、坑二：在onCreateViewHolder的时候会崩，提示：has a child,you shou remove 

3、坑三：当添加itemDecoration后，item的宽度不一致
下面是分析，如果想直接解决问题，可以使用下面的附件SpacingDecoration.java
http://stackoverflow.com/questions/30524599/items-are-not-the-same-width-when-using-recyclerview-gridlayoutmanager-to-make-c
SpacingDecoration.java

4、坑四：没有onItemClick和onItemLongClick，自己在ViewHoder添加onClickListenner吧，getPosition()、getLayoutPosition()、getAdapterPosition()还各种问题，获取不到position。
解决方案：http://stackoverflow.com/questions/24471109/recyclerview-onclick/26196831#26196831
可以直接使用下面这个附件 RecyclerViewClickListener

5.RecyclerView系列文章
http://blog.csdn.net/leejizhou/article/details/51179233

6.使用DiffUtil高效更新RecyclerView
http://blog.chengdazhi.com/index.php/231.

//日报
基本格局已经做完了.效果图如下
明天修改:
上拉新闻的时候隐藏非新闻部分.:ViewDragHelper
http://blog.csdn.net/zhangke3016/article/details/52347569
UI对齐的问题.
加入一张大图策略

7.分割线:

8.下拉刷新SwipeRefreshLayout


9.
http://ittiger.cn/2016/05/26/UC%E6%B5%8F%E8%A7%88%E5%99%A8%E9%A6%96%E9%A1%B5%E6%BB%91%E5%8A%A8%E5%8A%A8%E7%94%BB%E5%AE%9E%E7%8E%B0/



RecyclerView解析:http://zjutkz.net/2016/03/29/%E9%87%8D%E5%A4%8D%E9%80%A0%E8%BD%AE%E5%AD%90%E4%B9%9F%E6%98%AF%E6%9C%89%E6%84%8F%E4%B9%89%E7%9A%84%EF%BC%81PowerfulRecyclerView%E4%BD%BF%E7%94%A8%E6%8C%87%E5%AF%BC%E5%92%8C%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/

































