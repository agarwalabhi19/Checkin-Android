package com.checkin.app.checkin.Menu.UserMenu.Fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.checkin.app.checkin.Data.Resource;
import com.checkin.app.checkin.Menu.MenuItemInteraction;
import com.checkin.app.checkin.Menu.Model.MenuGroupModel;
import com.checkin.app.checkin.Menu.UserMenu.Adapter.MenuBestSellerAdapter;
import com.checkin.app.checkin.Menu.UserMenu.Adapter.MenuGroupAdapter;
import com.checkin.app.checkin.Menu.UserMenu.Adapter.MenuItemAdapter;
import com.checkin.app.checkin.Menu.UserMenu.MenuViewModel;
import com.checkin.app.checkin.Misc.BaseFragment;
import com.checkin.app.checkin.R;
import com.checkin.app.checkin.Utility.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.miguelcatalan.materialsearchview.utils.AnimationUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MenuGroupsFragment extends BaseFragment implements MenuGroupAdapter.OnGroupInteractionInterface {
    public static final String KEY_SESSION_STATUS = "menu.status";

    @BindView(R.id.rv_menu_groups)
    RecyclerView rvGroupsList;
    @BindView(R.id.container_as_menu_current_category)
    ViewGroup containerCurrentCategory;
    @BindView(R.id.tv_as_menu_current_category)
    TextView tvCurrentCategory;
    @BindView(R.id.shimmer_as_menu_group)
    ShimmerFrameLayout shimmerMenu;
    @BindView(R.id.sr_session_menu)
    SwipeRefreshLayout swipeRefreshLayout;

    private MenuViewModel mViewModel;
    private MenuGroupAdapter mAdapter;
    @Nullable
    private MenuItemInteraction mListener;
    private MenuBestSellerAdapter.SessionTrendingDishInteraction mBestsellerListener;
    private boolean mIsSessionActive = true;

    public MenuGroupsFragment() {
    }

    public static MenuGroupsFragment newInstance(SESSION_STATUS sessionStatus, MenuItemInteraction listener, MenuBestSellerAdapter.SessionTrendingDishInteraction bestsellerListener ) {
        MenuGroupsFragment fragment = new MenuGroupsFragment();
        fragment.mIsSessionActive = (sessionStatus == SESSION_STATUS.ACTIVE);
        fragment.mListener = listener;
        fragment.mBestsellerListener = bestsellerListener;
        return fragment;
    }

    @Override
    protected int getRootLayout() {
        return R.layout.fragment_as_menu_group;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupGroupRecycler();
        initRefreshScreen(R.id.sr_session_menu);

        mViewModel = ViewModelProviders.of(requireActivity()).get(MenuViewModel.class);

        mViewModel.getRecommendedItems().observe(this, listResource -> {
            if (listResource == null)
                return;

            if (listResource.getStatus() == Resource.Status.SUCCESS && listResource.getData() != null) {
                mAdapter.setData(listResource.getData());
//                shimmerBestSeller.stopShimmer();
//                shimmerBestSeller.setVisibility(View.GONE);
            }
        });

        mViewModel.getMenuGroups().observe(this, menuGroupResource -> {
            if (menuGroupResource == null) return;
            if (menuGroupResource.getStatus() == Resource.Status.SUCCESS && !menuGroupResource.isCached()) {
                stopRefreshing();
                setupData(menuGroupResource.getData());
            } else if (menuGroupResource.getStatus() == Resource.Status.LOADING) {
                startRefreshing();
                if (!mAdapter.hasData() && menuGroupResource.getData() != null)
                    setupData(menuGroupResource.getData());
            } else {
                stopRefreshing();
                Utils.toast(requireContext(), menuGroupResource.getMessage());
            }
        });

        mViewModel.getCurrentItem().observe(this, orderedItem -> {
            if (orderedItem == null) return;
            MenuItemAdapter.ItemViewHolder holder = orderedItem.getItemModel().getASItemHolder();

            if (holder != null && holder.getMenuItem() == orderedItem.getItemModel()) {
                holder.changeQuantity(mViewModel.getOrderedCount(orderedItem.getItemModel()) + orderedItem.getChangeCount());
            }
        });
    }

    private void setupData(List<MenuGroupModel> data) {
        mAdapter.setGroupList(data);
        if (shimmerMenu.getVisibility() == View.VISIBLE) {
            shimmerMenu.stopShimmer();
            shimmerMenu.setVisibility(View.GONE);
        }
        containerCurrentCategory.setVisibility(View.GONE);
    }

    private void setupGroupRecycler() {
        rvGroupsList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        mAdapter = new MenuGroupAdapter(null, mListener, this, mBestsellerListener);
        mAdapter.setSessionActive(mIsSessionActive);
        rvGroupsList.setAdapter(mAdapter);

        rvGroupsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (getView() != null) {
                    Integer topIndex = mAdapter.getTopExpandedGroupPosition();
                    if (topIndex != null) {
                        int fullHeight = getView().getHeight();
                        View view = rvGroupsList.getLayoutManager().findViewByPosition(topIndex);
                        int groupHeight = view != null ? view.getHeight() : 0;
                        if (groupHeight < fullHeight)
                            containerCurrentCategory.setVisibility(View.GONE);
                        else {
                            containerCurrentCategory.setVisibility(View.VISIBLE);
                            if (topIndex != tvCurrentCategory.getId())
                                tvCurrentCategory.setText(mAdapter.getGroupName(topIndex));
                        }
                    } else containerCurrentCategory.setVisibility(View.GONE);
                }
            }
        });
    }

    public boolean onBackPressed() {
        if (isGroupExpanded()) {
            mAdapter.contractView();
            return true;
        }
        return false;
    }

    private boolean isGroupExpanded() {
        return mAdapter.isGroupExpanded();
    }

    public void scrollToGroup(String title) {
        int pos = mAdapter.getGroupPosition(title);
        if (rvGroupsList.getLayoutManager() != null) {
            View v = rvGroupsList.getLayoutManager().findViewByPosition(pos);
            if (v == null) rvGroupsList.smoothScrollToPosition(pos);
            else {
                mAdapter.contractView();
                mAdapter.expandView(((MenuGroupAdapter.GroupViewHolder) rvGroupsList.getChildViewHolder(v)));
            }
        }
    }

    @Override
    protected void updateScreen() {
        mViewModel.updateResults();
    }

    @Override
    public void onGroupExpandCollapse(boolean isExpanded, MenuGroupModel groupModel) {
        if (isExpanded) {
            AnimationUtil.fadeInView(containerCurrentCategory);
            tvCurrentCategory.setText(groupModel.getName());
        } else {
            AnimationUtil.fadeOutView(containerCurrentCategory);
        }
    }

    @OnClick(R.id.container_as_menu_current_category)
    public void onStickyGroup() {
        mAdapter.contractView();
    }

    public enum SESSION_STATUS {
        ACTIVE, INACTIVE
    }
}