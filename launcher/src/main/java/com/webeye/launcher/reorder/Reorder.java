package com.webeye.launcher.reorder;


public class Reorder  {

	
	private ReoderInterface mReorder;
	
	public Reorder() {
	}


	public void setReorderAlgorithm(ReoderInterface reorder){
		mReorder = reorder;
	}
	
	/**
	 * 单页Z字排序
	 * @param occupied 要排序的数组
	 * @return 数组是否有任意元素移动了位置，true是有，false是没有
	 */
	public boolean reorder(SwapItem[][] occupied) {
		if (mReorder == null) {
			return false;
		}
		return mReorder.reorder(occupied);
	}


	public String printorder(SwapItem[][] occupied) {
		StringBuffer str = new StringBuffer();
		if(occupied == null){
			return "NULL";
		}
		int x_length = occupied.length;
		for(int x=0;x<x_length;x++){
			int y_length = occupied[x].length;
			for(int y=0;y<y_length;y++){
				str.append(occupied[x][y].t.toString()).append("      ");
			}
			str.append("\n");
		}
		return str.toString();
	}
	
	
	public  enum Type{noinit,empty,chessman,stone}
	public static class  SwapItem{

		public Type t;
		public Object item;
		public SwapItem(){
			t = Type.empty;
			item= null;
		}
		public SwapItem(Type t){
			this.t= t;
			item = null;
		}
		public SwapItem(Type t,final Object o){
			this.t = t;
			item = o;
		} 
	}
	
	//add by zhanggx1 for reordering all pages. s
	/**
	 * 多页Z字排序
	 * @param occupied 要排序的数组
	 * @param acrossPage 是否跨页排；true则将数组看作整体排序，前页有位置则后页补上
	 *        false是按每页排序，前页有位置则空着
	 * @return 数组是否有任意元素移动了位置，true是有，false是没有
	 */
	public boolean reorderAll(SwapItem[][][] occupied, boolean acrossPage) {
		if (mReorder == null) {
			return false;
		}
		return mReorder.reorderAll(occupied, acrossPage);
	}
	
	public boolean reorderAll(SwapItem[][][] occupied) {
		return reorderAll(occupied, false);
	}

	public String printAllOrder(SwapItem[][][] occupied) {
		StringBuffer str = new StringBuffer();
		if (occupied == null) {
			return "NULL";
		}
		final int screenCnt = occupied.length;
		final int cellXCnt = occupied[0].length;
		final int cellYCnt = occupied[0][0].length;
		
		for (int screen = 0; screen < screenCnt; screen++) {
			for (int cellY = 0; cellY < cellYCnt; cellY++) {
				for (int cellX = 0; cellX < cellXCnt; cellX++) {
					str.append(occupied[screen][cellX][cellY].t.toString()).append("   ");
				}
			}
		}
		return str.toString();
	}
	
	/**
	 * 单页反Z字排序
	 * @param occupied 要排序的数组
	 * @return 数组是否有任意元素移动了位置，true是有，false是没有
	 */
	public boolean reorderReverse(SwapItem[][] occupied) {
		if (mReorder == null) {
			return false;
		}
		return mReorder.reorderReverse(occupied);
	} 
	//add by zhanggx1 for reordering all pages. e

}
