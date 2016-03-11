package com.klauncher.reorder;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * 全部应用的Z排序
 * @author zhanggx1
 *
 */
public class All_Z_Reorder implements ReoderInterface {

	@Override
	public boolean reorder(Reorder.SwapItem[][] occupied) {
		if (occupied == null) {
			return false;
		}
		final List<Point> emptyArray = new ArrayList<Point>();
		
		final int cellXCnt = occupied.length;
		final int cellYCnt = occupied[0].length;
		
		boolean hasMove = false;
		for (int cellY = 0; cellY < cellYCnt; cellY++) {
			for (int cellX = 0; cellX < cellXCnt; cellX++) {
				hasMove |= handleOnePoint(occupied, emptyArray, cellX, cellY);
			}
		}
		return hasMove;
	}
	
	@Override
	public boolean reorderAll(Reorder.SwapItem[][][] occupied, boolean acrossPage) {
		if (occupied == null) {
			return false;
		}
		final List<Point3D> emptyArray = new ArrayList<Point3D>();
		
		final int screenCnt = occupied.length;
		final int cellXCnt = occupied[0].length;
		final int cellYCnt = occupied[0][0].length;
		
		boolean hasMove = false;
		for (int screen = 0; screen < screenCnt; screen++) {
			for (int cellY = 0; cellY < cellYCnt; cellY++) {
				for (int cellX = 0; cellX < cellXCnt; cellX++) {
					hasMove |= handleOnePoint3D(occupied, emptyArray, screen, cellX, cellY);
				}
			}
			if (!acrossPage) {
				emptyArray.clear();
			}
		}
		return hasMove;
	}
	
	@Override
	public boolean reorderReverse(Reorder.SwapItem[][] occupied) {
		if (occupied == null) {
			return false;
		}
		final List<Point> emptyArray = new ArrayList<Point>();
		
		final int cellXCnt = occupied.length;
		final int cellYCnt = occupied[0].length;
		
		boolean hasMove = false;
		for (int cellY = cellYCnt - 1; cellY >= 0; cellY--) {
			for (int cellX = cellXCnt - 1; cellX >= 0; cellX--) {
				hasMove |= handleOnePoint(occupied, emptyArray, cellX, cellY);
			}
		}
		return hasMove;
	}
	
	/**
	 * 处理一个2D节点，若是空节点，添加到空节点数组；若是可移动节点，移动该节点到首个空节点位置
	 * @param occupied 节点二维数组
	 * @param emptyArray 空节点数组
	 * @param cellX 当前节点的第一维
	 * @param cellY 当前节点的第二维
	 * @return 该节点是否有移动，true是有移动，false是无移动
	 */
	private boolean handleOnePoint(final Reorder.SwapItem[][] occupied,
			final List<Point> emptyArray,
			final int cellX, final int cellY) {
		if (!checkOnePoint(occupied, emptyArray, cellX, cellY)) {
			return false;
		}
		Reorder.SwapItem item = occupied[cellX][cellY];
		if (item.t == Reorder.Type.empty) {
			Point point = new Point(cellX, cellY);
			emptyArray.add(point);
		} else if (item.t == Reorder.Type.chessman && !emptyArray.isEmpty()) {
			Point empty = emptyArray.get(0);
			Reorder.SwapItem tmp = occupied[empty.x][empty.y];
			occupied[empty.x][empty.y] = item;
			occupied[cellX][cellY] = tmp;
			emptyArray.remove(0);
			
			Point point = new Point(cellX, cellY);
			emptyArray.add(point);
			return true;
		}
		return false;
	}
	
	private boolean checkOnePoint(final Reorder.SwapItem[][] occupied,
			final List<Point> emptyArray,
			final int cellX, final int cellY) {
		if (occupied == null
				|| emptyArray == null) {
			return false;
		}
		return checkOnePoint(occupied, cellX, cellY);
	}
	
	private boolean checkOnePoint(final Reorder.SwapItem[][] occupied, final int cellX, final int cellY) {
		if (cellX < 0
				|| cellX >= occupied.length
				|| cellY < 0
				|| cellY >= occupied[0].length) {
			return false;
		}
		return true;
	}
	
	/**
	 * 处理一个3D节点，若是空节点，添加到空节点数组；若是可移动节点，移动该节点到首个空节点位置
	 * @param occupied 节点三维数组
	 * @param emptyArray 空节点数组
	 * @param screen 当前节点的第一维
	 * @param cellX 当前节点的第二维
	 * @param cellY 当前节点的第三维
	 * @return 该节点是否有移动，true是有移动，false是无移动
	 */
	private boolean handleOnePoint3D(final Reorder.SwapItem[][][] occupied,
			final List<Point3D> emptyArray,
			final int screen, final int cellX, final int cellY) {
		if (!checkOnePoint3D(occupied, emptyArray, screen, cellX, cellY)) {
			return false;
		}
		Reorder.SwapItem item = occupied[screen][cellX][cellY];
		if (item == null) {
		    return false;
		}
		if (item.t == Reorder.Type.empty) {
			Point3D point = new Point3D(screen, cellX, cellY);
			emptyArray.add(point);
		} else if (item.t == Reorder.Type.chessman && !emptyArray.isEmpty()) {
			Point3D empty = emptyArray.get(0);
			Reorder.SwapItem tmp = occupied[empty.screen][empty.x][empty.y];
			occupied[empty.screen][empty.x][empty.y] = item;
			occupied[screen][cellX][cellY] = tmp;
			emptyArray.remove(0);
			
			Point3D point = new Point3D(screen, cellX, cellY);
			emptyArray.add(point);
			return true;
		}
		return false;
	}
	
	private boolean checkOnePoint3D(final Reorder.SwapItem[][][] occupied,
			final List<Point3D> emptyArray,
			final int screen, final int cellX, final int cellY) {
		if (occupied == null
				|| emptyArray == null) {
			return false;
		}
		return checkOnePoint3D(occupied, screen, cellX, cellY);
	}
	
	private boolean checkOnePoint3D(final Reorder.SwapItem[][][] occupied,
			final int screen, final int cellX, final int cellY) {
		if (screen < 0 || screen >= occupied.length) {
			return false;
		}
		return checkOnPoint3D(occupied, cellX, cellY);
	}
	
	private boolean checkOnPoint3D(final Reorder.SwapItem[][][] occupied, final int cellX, final int cellY) {
		if (cellX < 0
				|| cellX >= occupied[0].length
				|| cellY < 0
				|| cellY >= occupied[0][0].length) {
			return false;
		}
		return true;
	}
}
