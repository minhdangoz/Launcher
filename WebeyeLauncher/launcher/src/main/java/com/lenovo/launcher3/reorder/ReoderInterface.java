package com.lenovo.launcher3.reorder;

import com.lenovo.launcher3.reorder.Reorder.SwapItem;


interface ReoderInterface {
	/**
	 * 单页Z字排序
	 * @param occupied 要排序的数组
	 * @return 数组是否有任意元素移动了位置，true是有，false是没有
	 */
	boolean reorder(SwapItem[][] occupied);
	/**
	 * 多页Z字排序
	 * @param occupied 要排序的数组
	 * @param acrossPage 是否跨页排；true则将数组看作整体排序，前页有位置则后页补上
	 *        false是按每页排序，前页有位置则空着
	 * @return 数组是否有任意元素移动了位置，true是有，false是没有
	 */
	boolean reorderAll(SwapItem[][][] occupied, boolean acrossPage);
	/**
	 * 单页反Z字排序
	 * @param occupied 要排序的数组
	 * @return 数组是否有任意元素移动了位置，true是有，false是没有
	 */
	boolean reorderReverse(SwapItem[][] occupied);
}