package com.example.myapplication.fragments.Drawer.usersList;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.fragments.Drawer.feed.FeedFragment;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.R;

import com.example.myapplication.model.User;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;


public class UsersListFragment extends Fragment {
    RecyclerView list;
    List<User> data = new LinkedList<User>();
    FriendsListAdapter adapter;
    UsersListViewModel viewModel;
    LiveData<List<User>> liveData;


    public interface Delegate {
        void onItemSelected(User user);
    }

    Delegate parent;

        public UsersListFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FeedFragment.Delegate) {
            parent = (UsersListFragment.Delegate) getActivity();
        } else {
            throw new RuntimeException(context.toString()
                    + "student list parent activity must implement dtudent ;list fragment Delegate");
        }

        setHasOptionsMenu(true);        // create one instance of viewModel
        viewModel = new ViewModelProvider(this).get(UsersListViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);

        list = view.findViewById(R.id.friends_list_recycleView);
        list.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(layoutManager);

        adapter = new FriendsListAdapter();
        list.setAdapter(adapter);

        // click on specific friend
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                User user = data.get(position);
                parent.onItemSelected(user);
            }
        });


        liveData = viewModel.getData();
        // when tha values in liveData changes this function observes
        liveData.observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                data = users;
                adapter.notifyDataSetChanged(); //refresh
            }
        });

        final SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.friends_list_swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.refresh(new UserModel.CompListener() {
                    @Override
                    public void onComplete() {
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parent = null;
    }


    static class FriendsRowViewHolder extends RecyclerView.ViewHolder {
        TextView dogName;
        TextView ownerName;
        ImageView userImage;

        public FriendsRowViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            dogName = itemView.findViewById(R.id.user_list_dogName);
            ownerName = itemView.findViewById(R.id.user_list_ownerName);
            userImage = itemView.findViewById(R.id.user_list_userImg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onClick(position);
                        }
                    }
                }
            });
        }

        public void bind(User u) {
            dogName.setText(u.dogName);
            ownerName.setText(u.ownerName);
            if (u.imgUrl != null && !u.imgUrl.equals("")) {
                Picasso.get().load(u.imgUrl).placeholder(R.drawable.f).into(userImage);
            } else {
                userImage.setImageResource(R.drawable.f);
            }
        }
    }

    interface OnItemClickListener {
        void onClick (int position);
    }

    class FriendsListAdapter extends RecyclerView.Adapter<FriendsRowViewHolder>{
        private OnItemClickListener listener;

        void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }


        @NonNull
        @Override
        public FriendsRowViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.user_list_row, viewGroup,false );
            FriendsRowViewHolder vh = new FriendsRowViewHolder(v, listener);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsRowViewHolder friendRowViewHolder, int position) {
            User u = data.get(position);
            friendRowViewHolder.bind(u);

        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

}