package com.checkin.app.checkin.Menu.ActiveSessionMenu;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkin.app.checkin.Menu.ActiveSessionMenu.Fragment.ActiveSessionItemCustomizationFragment;
import com.checkin.app.checkin.Menu.Fragment.ItemCustomizationFragment;
import com.checkin.app.checkin.Menu.ActiveSessionMenu.Fragment.MenuCartFragment;
import com.checkin.app.checkin.Menu.Fragment.MenuInfoFragment;
import com.checkin.app.checkin.Menu.MenuItemInteraction;
import com.checkin.app.checkin.Menu.MenuViewModel;
import com.checkin.app.checkin.Menu.Model.MenuItemModel;
import com.checkin.app.checkin.Menu.ActiveSessionMenu.Fragment.ActiveSessionMenuFilterFragment;
import com.checkin.app.checkin.Menu.ActiveSessionMenu.Fragment.ActiveSessionMenuGroupsFragment;
import com.checkin.app.checkin.Menu.ActiveSessionMenu.Fragment.ActiveSessionMenuItemSearchFragment;
import com.checkin.app.checkin.Misc.BaseActivity;
import com.checkin.app.checkin.R;
import com.checkin.app.checkin.Utility.OnBoardingUtils;
import com.checkin.app.checkin.Utility.Utils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.checkin.app.checkin.Menu.Fragment.MenuGroupsFragment.KEY_SESSION_STATUS;
import static com.checkin.app.checkin.Menu.SessionMenuActivity.KEY_RESTAURANT_PK;
import static com.checkin.app.checkin.Menu.SessionMenuActivity.KEY_SESSION_PK;
import static com.checkin.app.checkin.Menu.SessionMenuActivity.KEY_SESSION_TRENDING_ITEM;
import static com.checkin.app.checkin.Menu.SessionMenuActivity.SESSION_ARG;

public class ActiveSessionMenuActivity extends BaseActivity implements
        MenuItemInteraction, ActiveSessionItemCustomizationFragment.ItemCustomizationInteraction, ActiveSessionMenuFilterFragment.MenuFilterInteraction {
    public static final String SP_MENU_SEARCH = "sp.menu.search";
    public static final String SP_MENU_CART = "sp.menu.cart";
    @BindView(R.id.rv_menu)
    RecyclerView rvMenu;
    @BindView(R.id.view_as_menu_search)
    MaterialSearchView vMenuSearch;
    @BindView(R.id.tv_as_menu_drinks)
    TextView drinksCategory;
    @BindView(R.id.tv_as_menu_food)
    TextView foodCategory;
    @BindView(R.id.tv_as_menu_dessert)
    TextView dessertCategory;
    @BindView(R.id.tv_as_menu_specials)
    TextView specialCategory;
    @BindView(R.id.tv_menu_count_ordered_items)
    TextView tvCountOrderedItems;
    @BindView(R.id.tv_as_menu_cart_item_price)
    TextView tvCartSubtotal;
    @BindView(R.id.container_as_menu_cart)
    ViewGroup menuCart;

    @BindView(R.id.btn_as_menu_search)
    ImageView btnMenuSearch;
    @BindView(R.id.btn_as_menu_filter)
    ImageView btnMenuFilter;

    private ActiveSessionMenuGroupsFragment.SESSION_STATUS mSessionStatus;

    private ActiveSessionMenuGroupsFragment mMenuFragment;
    private MenuCartFragment mCartFragment;
    private ActiveSessionMenuItemSearchFragment mSearchFragment;
    private ActiveSessionMenuFilterFragment mFilterFragment;
    private MenuViewModel mViewModel;

    private MenuBestSellerAdapter mBestSellerAdapter;


    public static void startWithSession(Context context, Long restaurantPk, @Nullable Long sessionPk, @Nullable Long itemModel) {
        context.startActivity(withSession(context, restaurantPk, sessionPk, itemModel));
    }

    public static Intent withSession(Context context, Long restaurantPk, @Nullable Long sessionPk, @Nullable Long itemModel) {
        Intent intent = new Intent(context, ActiveSessionMenuActivity.class);
        Bundle args = new Bundle();
        args.putSerializable(KEY_SESSION_STATUS, ActiveSessionMenuGroupsFragment.SESSION_STATUS.ACTIVE);
        args.putLong(KEY_RESTAURANT_PK, restaurantPk);
        if (sessionPk != null)
            args.putLong(KEY_SESSION_PK, sessionPk);
        if (itemModel != null)
            args.putLong(KEY_SESSION_TRENDING_ITEM, itemModel);
        intent.putExtra(SESSION_ARG, args);
        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_menu_new);
        ButterKnife.bind(this);

//        vpCategories.setAdapter(new MenuCategoriesFragmentAdapter(getSupportFragmentManager()));
//        tabLayout.setupWithViewPager(vpCategories);
//        setupTabIcons();

        Bundle args = getIntent().getBundleExtra(SESSION_ARG);
        mSessionStatus = (ActiveSessionMenuGroupsFragment.SESSION_STATUS) args.getSerializable(KEY_SESSION_STATUS);
        mViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
        mViewModel.fetchAvailableMenu(args.getLong(KEY_RESTAURANT_PK));
        long sessionPk = args.getLong(KEY_SESSION_PK, 0L);
        if (sessionPk > 0L)
            mViewModel.manageSession(sessionPk);

        mSearchFragment = ActiveSessionMenuItemSearchFragment.newInstance(ActiveSessionMenuActivity.this, true);

        init(R.id.container_as_menu_fragment, true);
        setupUiStuff();
        setUpObserver();
        setupMenuFragment();
        setupSearch();



//        rvMenu.setLayoutManager(new GridLayoutManager(this,2));
//        mBestSellerAdapter = new MenuBestSellerAdapter();
//        rvMenu.setAdapter(mBestSellerAdapter);
//
//        List<MenuBestSellerModel> data = new ArrayList<>();
//        mBestSellerAdapter.setData(data);
    }

    @OnClick({R.id.tv_as_menu_drinks, R.id.tv_as_menu_food, R.id.tv_as_menu_dessert, R.id.tv_as_menu_specials})
    public void onClickOfCategories(View v) {
        showAllCategories();
        switch (v.getId()) {
            case R.id.tv_as_menu_drinks:
                drinksCategory.setBackground(getResources().getDrawable(R.drawable.rectangle_red_stroke_white_fill));
                mViewModel.filterMenuCategories("Drinks");
                break;
            case R.id.tv_as_menu_food:
                foodCategory.setBackground(getResources().getDrawable(R.drawable.rectangle_red_stroke_white_fill));
                mViewModel.filterMenuCategories("Food");
                break;
            case R.id.tv_as_menu_dessert:
                dessertCategory.setBackground(getResources().getDrawable(R.drawable.rectangle_red_stroke_white_fill));
                mViewModel.filterMenuCategories("Dessert");
                break;
            case R.id.tv_as_menu_specials:
                specialCategory.setBackground(getResources().getDrawable(R.drawable.rectangle_red_stroke_white_fill));
                mViewModel.filterMenuCategories("Specials");
                break;
            default:
                mViewModel.resetMenuGroups();
                break;
        }
    }

    private void setUpObserver(){
        mViewModel.fetchTrendingItem();
        mViewModel.getTotalOrderedCount().observe(this, count -> {
            if (count == null)
                return;
            if (count > 0) {
                tvCountOrderedItems.setText(Utils.formatCount(count));
                tvCountOrderedItems.setVisibility(View.VISIBLE);
                explainCartMenu();
            } else {
                menuCart.setVisibility(View.GONE);
                tvCountOrderedItems.setVisibility(View.GONE);
            }
        });

        mViewModel.getOrderedSubTotal().observe(this, subtotal -> {
            if (subtotal == null)
                return;
            if(subtotal < 0.0){
                menuCart.setVisibility(View.GONE);
            }else {
                menuCart.setVisibility(View.VISIBLE);
                tvCartSubtotal.setText(Utils.formatCurrencyAmount(this, subtotal));
            }

        });

        mViewModel.mOriginalMenuGroups.observe(this, listResource -> {});
    }

    private void setupMenuFragment() {
        mMenuFragment = ActiveSessionMenuGroupsFragment.newInstance(mSessionStatus, this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_as_menu, mMenuFragment)
                .commit();

        mViewModel.mSelectedCategory.observe(this, s -> {
            if(s.equalsIgnoreCase("default")) {
                btnMenuFilter.setVisibility(View.VISIBLE);
                showAllCategories();
            }else {
                btnMenuFilter.setVisibility(View.GONE);
            }
        });
    }

    private void showAllCategories(){
        drinksCategory.setBackground(null);
        foodCategory.setBackground(null);
        dessertCategory.setBackground(null);
        specialCategory.setBackground(null);
    }

    private void setupFilter() {
        mFilterFragment = ActiveSessionMenuFilterFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_as_menu_fragment, mFilterFragment)
                .commit();
    }

    @OnClick(R.id.btn_as_menu_filter)
    public void showFilter(){
        setupFilter();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setupUiStuff() {
        Toolbar toolbar = findViewById(R.id.toolbar_menu);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setElevation(0);
        }
        mCartFragment = MenuCartFragment.newInstance(this);
        menuCart.setVisibility(View.GONE);
    }

    private void explainMenu() {
        OnBoardingUtils.conditionalOnBoarding(this, SP_MENU_SEARCH, true, new OnBoardingUtils.OnBoardingModel("Search for food item here.", btnMenuSearch, true));
    }

    private void setupCart() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_as_menu_fragment, mCartFragment)
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.container_as_menu_cart)
    public void onCartClick() {
            setupCart();
    }

    private void explainCartMenu() {
        OnBoardingUtils.conditionalOnBoarding(this, SP_MENU_CART, true, new OnBoardingUtils.OnBoardingModel("Checkout your order here.", menuCart, false));
    }

    private void setupSearch() {
        vMenuSearch.setVoiceSearch(true);
        vMenuSearch.setStartFromRight(false);
        vMenuSearch.setCursorDrawable(R.drawable.color_cursor_white);
        vMenuSearch.setBackIconGone();

        vMenuSearch.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.searchMenuItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mViewModel.searchMenuItems(query);
                return true;
            }

            @Override
            public boolean onQueryClear() {
                mViewModel.resetMenuItems();
                return true;
            }
        });
        vMenuSearch.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                mViewModel.resetMenuItems();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container_as_menu_fragment, mSearchFragment, "search")
                        .commit();
            }

            @Override
            public void onSearchViewClosed() {
                getSupportFragmentManager().beginTransaction()
                        .remove(mSearchFragment)
                        .commit();
            }
        });
    }

    @OnClick({R.id.btn_as_menu_search, R.id.container_as_menu_serach})
    public void onSearchClicked(View view) {
        vMenuSearch.showSearch();
    }

    @Override
    public void onBackPressed() {
        if(mViewModel.mSelectedCategory.getValue()!= null && !mViewModel.mSelectedCategory.getValue().equalsIgnoreCase("default")){
            mViewModel.resetMenuGroups();
            showAllCategories();
        }else if (vMenuSearch.isSearchOpen()) {
            closeSearch();
        } else if (mCartFragment.isVisible()) {
            mCartFragment.onBackPressed();
            return;
        }else if(mFilterFragment!=null && mFilterFragment.onBackPressed()  ){
            getSupportFragmentManager().beginTransaction()
                    .remove(mFilterFragment)
                    .commit();
        }else if(!mMenuFragment.onBackPressed()){
            super.onBackPressed();
        }else {
            super.onBackPressed();
        }
    }

    public void closeSearch() {
        vMenuSearch.closeSearch();
    }

    private boolean isSessionActive() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    vMenuSearch.setQuery(searchWrd, true);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onItemInteraction(MenuItemModel item, int count) {
        if (item.isComplexItem()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_as_menu_fragment, ActiveSessionItemCustomizationFragment.newInstance(item, this), "item_customization")
                    .commit();
        } else {
            mViewModel.orderItem();
        }
    }


    @Override
    public boolean onMenuItemAdded(MenuItemModel item) {
        if (isSessionActive()) {
            mViewModel.newOrderedItem(item);
            this.onItemInteraction(item, 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemChanged(MenuItemModel item, int count) {
        if (isSessionActive()) {
            if (mViewModel.updateOrderedItem(item, count)) {
                this.onItemInteraction(item, count);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onMenuItemShowInfo(MenuItemModel item) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container_as_menu_fragment, MenuInfoFragment.newInstance(item), "item_info")
                .commit();
    }

    @Override
    public int getItemOrderedCount(MenuItemModel item) {
        return mViewModel.getOrderedCount(item);
    }

    @Override
    public void onCustomizationDone() {
        mViewModel.orderItem();
    }

    @Override
    public void onCustomizationCancel() {
        mViewModel.cancelItem();
    }

    @Override
    public void onShowFilter() {

    }

    @Override
    public void onHideFilter() {

    }

    @Override
    public void filterByCategory(String category) {
        mMenuFragment.scrollToCategory(category);
    }

    @Override
    public void sortItems() {
        vMenuSearch.showSearch(true);
    }

    @Override
    public void resetFilters() {
        vMenuSearch.closeSearch();
    }

    @Override
    public void filterByAvailableMeals() {
        setupMenuFragment();
    }

}