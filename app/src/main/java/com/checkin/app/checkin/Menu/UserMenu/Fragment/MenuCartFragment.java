package com.checkin.app.checkin.Menu.UserMenu.Fragment;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.checkin.app.checkin.Data.Resource;
import com.checkin.app.checkin.Menu.MenuItemInteraction;
import com.checkin.app.checkin.Menu.Model.OrderedItemModel;
import com.checkin.app.checkin.Menu.UserMenu.Adapter.MenuTreatYourselfAdapter;
import com.checkin.app.checkin.Menu.UserMenu.Adapter.MenuCartAdapter;
import com.checkin.app.checkin.Menu.UserMenu.MenuViewModel;
import com.checkin.app.checkin.Misc.BaseFragment;
import com.checkin.app.checkin.R;
import com.checkin.app.checkin.Utility.Utils;
import com.checkin.app.checkin.session.model.TrendingDishModel;

import butterknife.BindView;
import butterknife.OnClick;

public class MenuCartFragment extends BaseFragment implements MenuCartAdapter.MenuCartInteraction, MenuTreatYourselfAdapter.TreatYourselfInteraction {
    @BindView(R.id.rv_menu_cart)
    RecyclerView rvCart;
    @BindView(R.id.rv_menu_treat_yourself)
    RecyclerView rvTreatYourself;
    @BindView(R.id.tv_menu_count_ordered_items)
    TextView tvCountItems;
    @BindView(R.id.tv_menu_subtotal)
    TextView tvCartSubtotal;
    @BindView(R.id.btn_menu_cart_proceed)
    Button btnCartProceed;
    @Nullable
    private MenuItemInteraction mListener;


    private MenuViewModel mViewModel;
    private MenuCartAdapter mCartAdapter;
    private MenuTreatYourselfAdapter mTreatYourselfAdapter;

    public MenuCartFragment() {
    }

    public static MenuCartFragment newInstance(MenuItemInteraction listener) {
        MenuCartFragment fragment = new MenuCartFragment();
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(requireActivity()).get(MenuViewModel.class);

        mCartAdapter = new MenuCartAdapter(this);
        rvCart.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        rvCart.setAdapter(mCartAdapter);

        mTreatYourselfAdapter = new MenuTreatYourselfAdapter(this);
        rvTreatYourself.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        rvTreatYourself.setAdapter(mTreatYourselfAdapter);

        mViewModel.getOrderedItems().observe(this, mCartAdapter::setOrderedItems);

        mViewModel.getTotalOrderedCount().observe(this, count -> {
            if (count == null)
                return;
            tvCountItems.setText(String.format("%s Items", Utils.formatCount(count)));
        });
        mViewModel.getOrderedSubTotal().observe(this, subtotal -> {
            if (subtotal == null)
                return;
            tvCartSubtotal.setText(Utils.formatCurrencyAmount(requireContext(), subtotal));
        });

        mViewModel.getServerOrderedItems().observe(this, resource -> {
            if (resource == null)
                return;
            if (resource.status == Resource.Status.SUCCESS) {
                Utils.toast(requireContext(), "Confirmed orders!");
                requireActivity().finish();
            } else if (resource.status == Resource.Status.LOADING) {
            } else {
                Utils.toast(requireContext(), resource.message);
                btnCartProceed.setEnabled(true);
            }
        });

        mViewModel.getMenuTrendingItems().observe(this, inventoryItemModels -> {
            if (inventoryItemModels == null)
                return;

            if (inventoryItemModels.status == Resource.Status.SUCCESS && inventoryItemModels.data != null)
                mTreatYourselfAdapter.setData(inventoryItemModels.data);
        });
    }

    @Override
    protected int getRootLayout() {
        return R.layout.fragment_menu_cart;
    }

    @OnClick(R.id.btn_menu_cart_proceed)
    public void onProceedBtnClicked(View view) {
        if (mCartAdapter.getItemCount() > 0) {
            btnCartProceed.setEnabled(false);
            mViewModel.confirmOrder();
        } else {
            Utils.toast(requireContext(), "Order something before proceeding!");
            btnCartProceed.setEnabled(true);
        }
    }

    @Override
    public void onOrderedItemChanged(OrderedItemModel item, int count) {
        item.setQuantity(count);
        mViewModel.setCurrentItem(item);
        mViewModel.orderItem();
    }

    @Override
    public void onOrderedItemRemark(OrderedItemModel item, String s) {
        if (s.length() > 0)
            item.setRemarks(s);
        mViewModel.setCurrentItem(item);
        mViewModel.orderItem();
    }

    private AlertDialog setupRemarksDialog(OrderedItemModel item) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(item.getRemarks());

        return new AlertDialog.Builder(requireContext())
                .setTitle("Enter Remarks")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    item.setRemarks(input.getText().toString());
                    item.setChangeCount(0);
                    mViewModel.setCurrentItem(item);
                    mViewModel.orderItem();
                    input.setText("");
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                    input.setText("");
                })
                .create();
    }

    @OnClick(R.id.im_menu_cart_back)
    public void onBack() {
        onBackPressed();
    }

    public boolean onBackPressed() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public void onTreatYourselfItemClick(TrendingDishModel data) {
        mViewModel.searchMenuItemById(data.getPk());
        mViewModel.getTreatMenuItem().observe(this, itemModel -> {
            if (itemModel != null && mListener != null) {
                mListener.onMenuItemAdded(itemModel);
            }
        });
    }
}

