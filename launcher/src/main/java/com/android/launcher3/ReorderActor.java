package com.android.launcher3;

import android.graphics.Point;
import android.view.View;

import com.webeye.launcher.ext.LauncherLog;
import com.webeye.launcher.reorder.All_Z_Reorder;
import com.webeye.launcher.reorder.Reorder;
import com.webeye.launcher.reorder.Reorder.SwapItem;
import com.webeye.launcher.reorder.Reorder.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于整理的界面操作
 * @author zhanggx1
 *
 */
public class ReorderActor {
	
	private final String TAG = "ReorderActor";

	private PagedView mMainView;
	/**
     * 手动整理单页时，记录原位置的数组
     */
    private Point[][] mManualReorderOccupied;
    private ManualReorderState mManualReorderState = ManualReorderState.EMPTY;
	
	private ReorderingChangedListener mReorderChangedListener;
	private static final ArrayList<WholeOrderTask> mOrderPendingList = new ArrayList<WholeOrderTask>();
	
	private static final int REORDER_DURATION = 230;
	
	public ReorderActor(PagedView mainView) {
		mMainView = mainView;
	}
	
	private boolean checkMainView() {
		if (mMainView == null) {
			return false;
		}
		boolean mRet = true;
		int childCnt = mMainView.getChildCount();
		for (int i = 0; i < childCnt; i++) {
			mRet &= checkMainViewAtPage(i);
		}
		return mRet;
	}
	
	private boolean checkMainViewAtPage(int page) {
		if (mMainView == null) {
			return false;
		}
		View child = mMainView.getChildAt(page);
		return (child instanceof CellLayout);
	}
    
    /**
     * 多指上滑时，手动整理的逻辑
     */
    public void reorderItemsManualUp() {
    	LauncherLog.i(TAG, "----------->reorderItemsManualPositive------------" + mManualReorderState);
    	switch (mManualReorderState) {
    		case EMPTY:
    			reorderItemsManualPositive();
    			break;
    		case POSITIVE:
    			break;
    		case REVERSE:
    			restoreItemsManual();
    			break;
    		default:
    			break;
    	}
    	//不要恢复
    	cleanReorderManual();
    }
    
    /**
     * 多指下滑时，手动整理的逻辑
     */
    public void reorderItemsManualDown() {
    	LauncherLog.i(TAG, "----------->reorder--down------------" + mManualReorderState);
    	switch (mManualReorderState) {
    		case EMPTY:
    			reorderItemsManualReverse();
    			break;
    		case REVERSE:
    			break;
    		case POSITIVE:
    			restoreItemsManual();
    			break;
    		default:
    			break;
    	}
    	//不要恢复
    	cleanReorderManual();
    }
    
    /**
     * 手动Z字整理
     */
    public void reorderItemsManualPositive(int page) {
    	if (!checkMainViewAtPage(page)) {
    		return;
    	}
    	boolean isCurrent = (page == mMainView.getCurrentPage());
    	final CellLayout layout = (CellLayout)mMainView.getChildAt(0);
    	if (layout == null) {
    		return;
    	}
    	((Launcher)mMainView.getContext()).setAnimating(true, "reorder");
    	
    	final int mCountX = layout.getCountX();
    	final int mCountY = layout.getCountY();
    	
    	int taskNum = mCountY * mCountX;
    	final WholeOrderTask orderTask = new WholeOrderTask(taskNum);
    	final OrderRunnable countEnd  = new OrderRunnable(orderTask);
    	mOrderPendingList.add(orderTask);
    	
    	SwapItem[][] occupied = getOccupiedInfoAtPage(page, mCountX, mCountY);
    	    	
    	if (isCurrent) {
	    	mManualReorderOccupied = new Point[mCountX][mCountY];
	    	mManualReorderState = ManualReorderState.POSITIVE;
    	}
		
		//reorder
		Reorder r = new Reorder();
//		Log.d("dooba", r.printorder(occupied));
		r.setReorderAlgorithm(new All_Z_Reorder());
		boolean hasMove = r.reorder(occupied);
		
//		Log.d("dooba", "/n=======================================/n");
//		Log.d("dooba", r.printorder(occupied));
		
		if (!hasMove) {
			if (isCurrent) {
				mManualReorderOccupied = null;
				mManualReorderState = ManualReorderState.EMPTY;
			}
			orderTask.resetTask();
			return;
		}
		int delay = 0;
        float delayAmount = 30;
		for (int cellY = 0; cellY < mCountY; cellY++) {
			for (int cellX = 0; cellX < mCountX; cellX++) {
				orderAnItemManual(occupied, cellX, cellY, countEnd, delay, delayAmount);
			}
		}
	}
    
    /**
     * 手动Z字整理
     */
    private void reorderItemsManualPositive() {
    	reorderItemsManualPositive(mMainView.getCurrentPage());
	}
    
    /**
     * 手动反Z字整理
     */
    public void reorderItemsManualReverse(int page) {
    	if (!checkMainViewAtPage(page)) {
    		return;
    	}
    	boolean isCurrent = (page == mMainView.getCurrentPage());
    	final CellLayout layout = (CellLayout)mMainView.getChildAt(0);
    	if (layout == null) {
    		return;
    	}
    	((Launcher)mMainView.getContext()).setAnimating(true, "reorder");
    	
    	final int mCountX = layout.getCountX();
    	final int mCountY = layout.getCountY();
    	int taskNum = mCountY * mCountX;
    	final WholeOrderTask orderTask = new WholeOrderTask(taskNum);
    	final OrderRunnable countEnd  = new OrderRunnable(orderTask);
    	mOrderPendingList.add(orderTask);
    	
    	SwapItem[][] occupied = getOccupiedInfoAtPage(page, mCountX, mCountY);
    	
    	if (isCurrent) {
	    	mManualReorderOccupied = new Point[mCountX][mCountY];
	    	mManualReorderState = ManualReorderState.REVERSE;
    	}
		
		//reorder
		Reorder r = new Reorder();
//		Log.d("dooba", r.printorder(occupied));
		r.setReorderAlgorithm(new All_Z_Reorder());
		boolean hasMove = r.reorderReverse(occupied);
		
		if (!hasMove) {
			if (isCurrent) {
				mManualReorderOccupied = null;
				mManualReorderState = ManualReorderState.EMPTY;
			}
			orderTask.resetTask();
			return;
		}
		
//		Log.d("dooba", "/n=======================================/n");
//		Log.d("dooba", r.printorder(occupied));
		int delay = 0;
        float delayAmount = 30;		
		for (int cellY = mCountY - 1; cellY >= 0; cellY--) {
			for (int cellX = mCountX - 1; cellX >= 0; cellX--) {
				orderAnItemManual(occupied, cellX, cellY, countEnd, delay, delayAmount);
			}
		}
    }
    
    /**
     * 手动反Z字整理
     */
    private void reorderItemsManualReverse() {
    	reorderItemsManualReverse(mMainView.getCurrentPage());
    }
    
    /**
     * 恢复所有手动整理的数据
     */
    private void restoreItemsManual() {
		if (mManualReorderState == ManualReorderState.EMPTY
				|| mManualReorderOccupied == null) {
			mManualReorderOccupied = null;
			mManualReorderState = ManualReorderState.EMPTY;
			return;
		}
		((Launcher)mMainView.getContext()).setAnimating(true, "reorder");
		
		int countX = mManualReorderOccupied.length;
		int countY = mManualReorderOccupied[0].length;
		
		int taskNum = countX * countY;
    	final WholeOrderTask orderTask = new WholeOrderTask(taskNum);
    	final OrderRunnable countEnd  = new OrderRunnable(orderTask);
    	mOrderPendingList.add(orderTask);
		
    	int delay = 0;
        float delayAmount = 30;
		if (mManualReorderState == ManualReorderState.POSITIVE) {
			for (int cellY = countY - 1; cellY >= 0; cellY--) {
				for (int cellX = countX - 1; cellX >= 0; cellX--) {
					restoreAnItemManual(cellX, cellY, countEnd, delay, delayAmount);
				}
			}
		} else {
			for (int cellY = 0; cellY < countY; cellY++) {
				for (int cellX = 0; cellX < countX; cellX++) {
					restoreAnItemManual(cellX, cellY, countEnd, delay, delayAmount);
				}
			}
		}
		mManualReorderOccupied = null;
		mManualReorderState = ManualReorderState.EMPTY;
    }
    
    /**
     * 手动整理一个值到某个位置
     * @param occupied
     * @param cellX
     * @param cellY
     */
    private void orderAnItemManual(final SwapItem[][] occupied,
    		final int cellX, final int cellY, final OrderRunnable taskRunnable, int delay, float delayAmount) {
    	if (!checkAnItemManual(occupied, cellX, cellY)) {
    		executeOrderRunnable(taskRunnable);
			return;
		}
    	View item = (View)occupied[cellX][cellY].item;
		ItemInfo info = (ItemInfo)item.getTag();
		if (info != null
				&& (info.cellX != cellX
				|| info.cellY != cellY)) {			
			int screenOrder = mMainView.getScreenOrderById(info.screenId);
			if (screenOrder == -1) {
				executeOrderRunnable(taskRunnable);
				return;
			}
            if (screenOrder == mMainView.getCurrentPage()) {
			    mManualReorderOccupied[cellX][cellY] = new Point(info.cellX, info.cellY);
			}
			if (mMainView.animateChildToPosition(item, screenOrder, cellX, cellY,
					taskRunnable, REORDER_DURATION, delay, true)) {
				return;
			}
		}
		executeOrderRunnable(taskRunnable);
    }
    
    private boolean checkAnItemManual(final SwapItem[][] occupied,
    		final int cellX, final int cellY) {
    	if (!checkAnItemParamManual(occupied, cellX, cellY)) {
			return false;
		}
		if (occupied[cellX][cellY].item == null) {
			return false;
		}
		View item = (View)occupied[cellX][cellY].item;
		if (item.getTag() == null || !(item.getTag() instanceof ItemInfo)) {
			return false;
		}
		return true;
    }
    
    private void executeOrderRunnable(final OrderRunnable taskRunnable) {
    	if (taskRunnable != null) {
			taskRunnable.run();
		}
    }
    
    private boolean checkAnItemParamManual(final SwapItem[][] occupied,
    		final int cellX, final int cellY) {
    	if (occupied == null
				|| cellX < 0
				|| cellX >= occupied.length
				|| cellY < 0
				|| cellY >= occupied[0].length) {
			return false;
		}
    	return true;
    }
    
    /**
     * 手动恢复一个指到某个位置
     * @param cellX
     * @param cellY
     */
    private void restoreAnItemManual(final int cellX, final int cellY, final OrderRunnable taskRunnable, int delay, float delayAmount) {
    	if (!checkManualReorderByRestore(cellX, cellY)) {
    		executeOrderRunnable(taskRunnable);
			return;
		}
    	
    	Point p = mManualReorderOccupied[cellX][cellY];
		int screen = mMainView.getCurrentPage();
		CellLayout layout = (CellLayout)mMainView.getChildAt(screen);
		View item = layout.getChildAt(cellX, cellY);
		if (item == null 
				|| item.getTag() == null
				|| !(item.getTag() instanceof ItemInfo)) {
			executeOrderRunnable(taskRunnable);
			return;
		}
		
		ItemInfo itemInfo = (ItemInfo)item.getTag();
		if ((itemInfo.cellX != p.x
				|| itemInfo.cellY != p.y)
				&& (mManualReorderOccupied[p.x][p.y] == null)) {
			mManualReorderOccupied[cellX][cellY] = null;
			if (mMainView.animateChildToPosition(item, screen, p.x, p.y,
					taskRunnable, REORDER_DURATION, delay, true)) {
				return;
			}
		}
		executeOrderRunnable(taskRunnable);
    }
    
    private boolean checkManualReorderByRestore(final int cellX, final int cellY) {
    	if (mManualReorderOccupied == null
				|| cellX < 0
				|| cellX >= mManualReorderOccupied.length
				|| cellY < 0
				|| cellY >= mManualReorderOccupied[0].length) {
			return false;
		}
    	
    	Point p = mManualReorderOccupied[cellX][cellY];
		if (p == null) {
			return false;
		}
		return true;
    }
	
	/**
     * 清理手动整理的值记录
     */
    public void cleanReorderManual() {
    	mManualReorderOccupied = null;
    	mManualReorderState = ManualReorderState.EMPTY;
	}
    
    /**
     * 取得某页的单元格占用情况
     * @param screen
     * @return
     */
    private SwapItem[][] getOccupiedInfoAtPage(final int screen, final int mCountX, final int mCountY) {
    	CellLayout cellLayout = (CellLayout)mMainView.getChildAt(screen);
		SwapItem stone_Item = new SwapItem(Type.stone);
		SwapItem[][] occupied = new SwapItem[mCountX][mCountY];
		
		occupiedByEmpty(occupied, mCountX, mCountY);
		//init array
		for (int y = 0; y < mCountY; y++) {
			for (int x = 0; x < mCountX; x++) {
				getAnOccupiedInfo(cellLayout, x, y, mCountX, mCountY, occupied, stone_Item);
			}
		}
		return occupied;
    }
    
    private void getAnOccupiedInfo(final CellLayout cellLayout, final int x, final int y,
    		final int mCountX, final int mCountY, final SwapItem[][] occupied, final SwapItem stone_Item) {
    	View item = cellLayout.getChildAt(x, y);
		if (!checkView(item, mCountX, mCountY)) {
			return;
		}
		ItemInfo itemInfo = (ItemInfo)item.getTag();
		if (itemInfo instanceof ShortcutInfo || itemInfo instanceof FolderInfo) {
			occupied[itemInfo.cellX][itemInfo.cellY] = new SwapItem(Type.chessman, item);
		}
		if ((itemInfo instanceof LauncherAppWidgetInfo)
		        /*|| (itemInfo instanceof LenovoWidgetViewInfo) Lenovo-SW zhaoxin5 20150721 */) {
			getAppWidgetOccupiedInfoManual(itemInfo, item, mCountX, mCountY,
		    		occupied, stone_Item);
		}
    }
    
    private boolean checkView(final View item, final int mCountX, final int mCountY) {
		if (item == null || !(item.getTag() instanceof ItemInfo)) {
			return false;
		}
		ItemInfo itemInfo= (ItemInfo)item.getTag();
		if (itemInfo.cellX < 0
				|| itemInfo.cellX >= mCountX
				|| itemInfo.cellY < 0
				|| itemInfo.cellY >= mCountY) {
			return false;
		}
		return true;
    }
    
    private void occupiedByEmpty(SwapItem[][] occupied, final int mCountX, final int mCountY) {
    	SwapItem empty_Item = new SwapItem();
    	for (int y = 0; y < mCountY; y++) {
			for (int x = 0; x < mCountX; x++) {
				occupied[x][y] = empty_Item;
			}
		}
    }
    
    private void getAppWidgetOccupiedInfoManual(final ItemInfo itemInfo, 
    		final View item, final int mCountX, final int mCountY,
    		final SwapItem[][] occupied, final SwapItem stone_Item) {
    	if (itemInfo.spanX == 1 && itemInfo.spanY == 1) {
			occupied[itemInfo.cellX][itemInfo.cellY] = new SwapItem(Type.chessman, item);
		} else {
			int maxX = Math.min(mCountX, itemInfo.cellX + itemInfo.spanX);
			int maxY = Math.min(mCountY, itemInfo.cellY + itemInfo.spanY);
			for (int y1 = itemInfo.cellY; y1 < maxY; y1++) {
				for (int x1 = itemInfo.cellX ; x1 < maxX; x1++) {
					occupied[x1][y1] = stone_Item;
				}
			}
		}
    }
	
	/**
     * 取得所有页面的单元格占用情况
     * @return
     */
    private SwapItem[][][] getAllOccupiedInfo(final int mCountX, final int mCountY) {    	
    	CellLayout layout = null;
		SwapItem empty_Item = new SwapItem();
		SwapItem stone_Item = new SwapItem(Type.stone);
		final int pageCnt = mMainView.getChildCount();		
		SwapItem[][][] occupied = new SwapItem[pageCnt][mCountX][mCountY];
		List<Integer> noOrderScreens = mMainView.getNoOrderScreens();
		boolean allOrder = noOrderScreens == null || noOrderScreens.isEmpty();
		
		boolean reorder = false;
		for (int screen = 0; screen < pageCnt; screen++) {
			layout = (CellLayout) mMainView.getChildAt(screen);
			reorder = allOrder || !noOrderScreens.contains(screen);
			for (int y = 0; y < mCountY; y++) {
				for (int x = 0; x < mCountX; x++) {
					getAnItemOccupiedInfo(reorder, layout, empty_Item,
				    		mCountX, mCountY, screen, x, y, occupied, stone_Item);
					if (occupied[screen][x][y] == null) {
					    occupied[screen][x][y] = empty_Item;
					}
				}
			}
		}
		return occupied;
    }
    
    private void getAnItemOccupiedInfo(boolean reorder, CellLayout layout, SwapItem empty_Item,
    		final int mCountX, final int mCountY, final int screen,
    		final int x, final int y, final SwapItem[][][] occupied, final SwapItem stone_Item) {
    	if (!reorder) {
			occupied[screen][x][y] = stone_Item;
			return;
		}
    	View view = layout.getChildAt(x, y);
		if (view == null) {
		    occupied[screen][x][y] = empty_Item;
		    return;
		}
		ItemInfo itemInfo = (ItemInfo)view.getTag();
		if (itemInfo instanceof ShortcutInfo
				|| itemInfo instanceof FolderInfo) {
			occupied[screen][x][y] = new SwapItem(Type.chessman, view);
			return;
		}
		if ((itemInfo instanceof LauncherAppWidgetInfo)
		        /*|| (itemInfo instanceof LenovoWidgetViewInfo) Lenovo-SW zhaoxin5 20150721 */) {
			getAppWidgetOccupiedInfo(itemInfo, 
		    		view, mCountX, mCountY, screen, x, y, occupied, stone_Item);
			return;
		}
    }
    
    private void getAppWidgetOccupiedInfo(final ItemInfo itemInfo, 
    		final View view, final int mCountX, final int mCountY, final int screen,
    		final int x, final int y, final SwapItem[][][] occupied, final SwapItem stone_Item) {
    	if (itemInfo.spanX == 1 && itemInfo.spanY == 1) {
			occupied[screen][x][y] = new SwapItem(Type.chessman, view);
			return;
		}
    	int maxX = Math.min(mCountX, itemInfo.cellX + itemInfo.spanX);
		int maxY = Math.min(mCountY, itemInfo.cellY + itemInfo.spanY);
		for (int y1 = y; y1 < maxY; y1++) {
			for (int x1 = x ; x1 < maxX; x1++) {
				occupied[screen][x1][y1] = stone_Item;
			}
		}
    }
    
    /**
     * 多页Z字整理
     */
    public void reorderItemAllScreen(){
    	if (!checkMainView()) {
    		return;
    	}
    	
    	final int pageCnt = mMainView.getChildCount();
    	final CellLayout layout = (CellLayout)mMainView.getChildAt(0);
    	if (layout == null) {
    		return;
    	}
    	((Launcher)mMainView.getContext()).setAnimating(true, "reorder");
    	
    	final int mCountX = layout.getCountX();
    	final int mCountY = layout.getCountY();
    	int taskNum = pageCnt * mCountY * mCountX;
    	final WholeOrderTask orderTask = new WholeOrderTask(taskNum);
    	final OrderRunnable countEnd  = new OrderRunnable(orderTask);
    	mOrderPendingList.add(orderTask);
    	
    	SwapItem[][][] occupied = getAllOccupiedInfo(mCountX, mCountY);
		
		//reorder
		Reorder r = new Reorder();
		r.setReorderAlgorithm( new All_Z_Reorder());
		boolean hasMove = r.reorderAll(occupied);//默认是不跨页的
		
		if (!hasMove) {
			orderTask.resetTask();
			return;
		}
		
		for (int screen = 0; screen < pageCnt; screen++) {
			int[] delayArray = new int[] {0, 30};
			for (int cellY = 0; cellY < mCountY; cellY++) {
				for (int cellX = 0; cellX < mCountX; cellX++) {					
					reorderAnItemAllScreen(screen, cellX, cellY, occupied, orderTask,
				    		countEnd, delayArray);
				}
			}
		}
	}
    
    private void reorderAnItemAllScreen(final int screen, final int cellX,
    		final int cellY, final SwapItem[][][] occupied, final WholeOrderTask orderTask,
    		final OrderRunnable countEnd, final int[] delayArray) {
    	if (occupied[screen][cellX][cellY].item == null) {
			orderTask.removeATask();
			return;
		}
    	View item = (View)occupied[screen][cellX][cellY].item;
		if (item.getTag() == null
				|| !(item.getTag() instanceof ItemInfo)) {
			orderTask.removeATask();
			return;
		}
		ItemInfo info = (ItemInfo)item.getTag();
		if (info != null
				&& (info.screenId != mMainView.getScreenIdByOrder(screen)
				|| info.cellX != cellX
				|| info.cellY != cellY)) {
			boolean ret = mMainView.animateChildToPosition(item,
					screen, cellX, cellY, countEnd, REORDER_DURATION, delayArray[0], true);
			if (ret) {
				delayArray[0] += delayArray[1];
				delayArray[1] *= 0.9;
				return;
			}						
		}
		orderTask.removeATask();
    }
    
    public boolean isLeosReordering() {
    	boolean rt = mOrderPendingList.size() != 0;
		return rt;
	}
	
	public void setReorderingChangedListener(ReorderingChangedListener l) {
		mReorderChangedListener = l;
	}
	
	public interface ReorderingChangedListener {
		void onReorderEnd();
	}
	
	private class WholeOrderTask {
		private int mTaskNum = 0;
		
		public WholeOrderTask(int taskNum) {
			mTaskNum = taskNum;
		}
		
		public void removeATask() {
			if (mTaskNum <= 0) {
				return;
			}
			mTaskNum--;
			if (mTaskNum == 0) {
				resetTask();
			}
		}
		
		public void resetTask() {
			mTaskNum = 0;
			mOrderPendingList.remove(this);
			if (mReorderChangedListener != null) {
				mReorderChangedListener.onReorderEnd();
			}
		}
	}
	private static class OrderRunnable implements Runnable {
		private WholeOrderTask mOrderTask;
		
		public OrderRunnable(WholeOrderTask orderTask) {
			mOrderTask = orderTask;
		}		
		@Override
		public void run() {
			if (mOrderTask != null) {
				mOrderTask.removeATask();
			}
		}
	}
	
	private static enum ManualReorderState {
    	EMPTY, POSITIVE, REVERSE
    }
}
