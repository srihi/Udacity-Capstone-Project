package com.example.rajesh.udacitycapstoneproject.dashboard;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rajesh.udacitycapstoneproject.R;
import com.example.rajesh.udacitycapstoneproject.account.AccountActivity;
import com.example.rajesh.udacitycapstoneproject.base.frament.BaseFragment;
import com.example.rajesh.udacitycapstoneproject.category.CategoryEditActivity;
import com.example.rajesh.udacitycapstoneproject.expense.ExpenseActivity;
import com.example.rajesh.udacitycapstoneproject.expense.recurring.RecurringExpenseAdapter;
import com.example.rajesh.udacitycapstoneproject.realm.Account;
import com.example.rajesh.udacitycapstoneproject.realm.Expense;
import com.example.rajesh.udacitycapstoneproject.realm.table.RealmTable;
import com.example.rajesh.udacitycapstoneproject.utils.MonthTimeStamp;
import com.github.clans.fab.FloatingActionMenu;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;


public class DashBoardFragment extends BaseFragment {
    RecurringExpenseAdapter expenseAdapter;

    @Bind(R.id.rv_dashboard)
    RecyclerView rvDashBoard;

    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;

    @Bind(R.id.tv_total_expense)
    TextView tvTotalExpense;

    @Bind(R.id.tv_remaining_amount)
    TextView tvRemainingAmount;

    @Bind(R.id.progress_amount)
    ProgressBar progressAmount;

    @Bind(R.id.progress_expense)
    ProgressBar progressExpense;

    @Bind(R.id.fab_menu)
    FloatingActionMenu fabMenu;

    @Bind(R.id.ll_amount_container)
    LinearLayout llAmountContainer;

    @Bind(R.id.ll_money_calculator)
    LinearLayout llMoneyCalculator;

    @Bind(R.id.rl_no_account)
    RelativeLayout rlNoAccount;

    @Bind(R.id.rl_no_expense)
    RelativeLayout rlNoExpense;

    Double mExpenses = 0.0;
    Double mTotalAmount = 0.0;
    Double remainingAmount = 0.0;

    private Realm mRealm;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRealm = Realm.getDefaultInstance();

        fabMenu.setClosedOnTouchOutside(true);

        setRecyclerViewAdapter();

        RealmResults<Account> accounts = mRealm.where(Account.class).findAll();
        RealmResults<Expense> expenses = mRealm.where(Expense.class).findAll();

        if (accounts.size() <= 0) {
            rvDashBoard.setVisibility(View.GONE);
            llMoneyCalculator.setVisibility(View.GONE);
            rlNoAccount.setVisibility(View.VISIBLE);
        } else if (expenses.size() <= 0) {
            rvDashBoard.setVisibility(View.GONE);
            llMoneyCalculator.setVisibility(View.GONE);
            rlNoAccount.setVisibility(View.GONE);
            rlNoExpense.setVisibility(View.VISIBLE);
        } else {
            rvDashBoard.setVisibility(View.VISIBLE);
            llMoneyCalculator.setVisibility(View.VISIBLE);
            rlNoExpense.setVisibility(View.GONE);
            rlNoAccount.setVisibility(View.GONE);
            showMoneyCalculator();
        }
    }

    private void showMoneyCalculator() {
        tvRemainingAmount.setText("" + getRemainingAmount());

        progressAmount.setMax(new Double(mTotalAmount).intValue());
        progressAmount.setProgress(new Double(remainingAmount).intValue());

        progressExpense.setMax(new Double(mTotalAmount).intValue());
        progressExpense.setProgress(new Double(mExpenses).intValue());


        mTotalAmount = (Double) mRealm.where(Account.class)
                .greaterThanOrEqualTo(RealmTable.DATE, MonthTimeStamp.getStartTimeStamp(new Date()))
                .lessThanOrEqualTo(RealmTable.DATE, MonthTimeStamp.getEndTimeStamp(new Date())).sum(RealmTable.AMOUNT);

        mExpenses = (Double) mRealm.where(Expense.class)
                .greaterThan(RealmTable.DATE, MonthTimeStamp.getStartTimeStamp(new Date()))
                .lessThanOrEqualTo(RealmTable.DATE, MonthTimeStamp.getEndTimeStamp(new Date())).sum(RealmTable.AMOUNT);

        tvTotalAmount.setText(String.valueOf(mTotalAmount));
        tvTotalExpense.setText(String.valueOf(mExpenses));
        tvRemainingAmount.setText(String.valueOf(getRemainingAmount()));
    }

    private Double getRemainingAmount() {
        return remainingAmount = mTotalAmount - mExpenses;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_dash_board;
    }

    private void setRecyclerViewAdapter() {
        RealmResults<Expense> expenses = mRealm.where(Expense.class)
                .greaterThan(RealmTable.DATE, MonthTimeStamp.getStartTimeStamp(new Date()))
                .lessThanOrEqualTo(RealmTable.DATE, MonthTimeStamp.getEndTimeStamp(new Date())).findAll();
        rvDashBoard.setLayoutManager(new LinearLayoutManager(getActivity()));
        expenseAdapter = new RecurringExpenseAdapter(getActivity(), expenses);
        rvDashBoard.setAdapter(expenseAdapter);

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItem((int) expenseAdapter.getExpenseIdByPosition(viewHolder.getAdapterPosition()));
                expenseAdapter.notifyItemRangeRemoved(viewHolder.getAdapterPosition(), expenseAdapter.getItemCount());
                expenseAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                expenseAdapter.notifyDataSetChanged();
            }
        });
        swipeToDismissTouchHelper.attachToRecyclerView(rvDashBoard);
    }

    @OnClick({R.id.fab_add_expense, R.id.fab_add_account, R.id.fab_add_category, R.id.iv_no_account, R.id.iv_no_expense})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_account:
                getActivity().startActivity(AccountActivity.getLaunchIntent(getActivity(), null));
                break;
            case R.id.fab_add_expense:
                getActivity().startActivity(ExpenseActivity.getLaunchIntent(getActivity(), null));
                break;
            case R.id.fab_add_category:
                getActivity().startActivity(CategoryEditActivity.getLaunchIntent(getActivity(), null));
                break;
            case R.id.iv_no_account:
                getActivity().startActivity(AccountActivity.getLaunchIntent(getActivity(), null));
                break;
            case R.id.iv_no_expense:
                getActivity().startActivity(ExpenseActivity.getLaunchIntent(getActivity(), null));
                break;
        }
        fabMenu.close(true);
    }

    private void deleteItem(final int id) {
        mRealm.beginTransaction();
        Expense expense = mRealm.where(Expense.class).equalTo(RealmTable.ID, id).findFirst();
        expense.deleteFromRealm();
        mRealm.commitTransaction();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
