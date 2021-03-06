package com.team29.speakingpartners.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.team29.speakingpartners.R;
import com.team29.speakingpartners.model.RecentListModel;
import com.team29.speakingpartners.utils.GlideApp;
import com.team29.speakingpartners.utils.GlideOptions;

import javax.annotation.Nullable;

public class IncomingRecentListAdapter extends RecyclerView.Adapter<IncomingRecentListAdapter.RecentListViewHolder> {

    public static final String TAG = OutgoingRecentListAdapter.class.getSimpleName();

    private Context mContext;
    private List<RecentListModel> mLists;

    private RecentItemClickListener recentItemClickListener;

    public IncomingRecentListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public IncomingRecentListAdapter(Context mContext, List<RecentListModel> mLists) {
        this.mContext = mContext;
        this.mLists = mLists;
    }

    public IncomingRecentListAdapter(Context mContext, List<RecentListModel> mLists, RecentItemClickListener recentItemClickListener) {
        this.mContext = mContext;
        this.mLists = mLists;
        this.recentItemClickListener = recentItemClickListener;
    }

    public void setItemLists(List<RecentListModel> lists) {
        this.mLists = lists;
        notifyDataSetChanged();
    }

    public void setRecentItemClickListener(RecentItemClickListener listener) {
        this.recentItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecentListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recent_list_row, viewGroup, false);
        return new RecentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentListViewHolder recentListViewHolder, int i) {
        RecentListModel recentListModel = mLists.get(i);
        recentListViewHolder.bindView(recentListModel);
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public class RecentListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        RecentListModel mRecentListModel;

        AppCompatTextView recentUsername, recentTopic, recentDate;
        AppCompatImageView imgRecentUserProfile;
        AppCompatImageButton btnDelete;

        public RecentListViewHolder(@NonNull View itemView) {
            super(itemView);
            recentUsername = itemView.findViewById(R.id.recent_user_name);
            recentTopic = itemView.findViewById(R.id.recent_topic);
            recentDate = itemView.findViewById(R.id.recent_date);

            imgRecentUserProfile = itemView.findViewById(R.id.recent_user_img);

            btnDelete = itemView.findViewById(R.id.btn_recent_remove);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @SuppressLint("SetTextI18n")
        private void bindView(RecentListModel model) {
            this.mRecentListModel = model;

            fetchUserData(mRecentListModel.getFrom_email());

            recentTopic.setText(mRecentListModel.getReq_topic());
            recentDate.setText(mRecentListModel.getDateString() + "\n" + mRecentListModel.getTimeString());

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recentItemClickListener.setOnItemLongClick(mRecentListModel);
                }
            });
        }

        private void fetchUserData(String email) {
            FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("email", email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "Listen Error");
                                return;
                            }

                            for (QueryDocumentSnapshot snapshot : snapshots) {
                                recentUsername.setText(snapshot.getString("user_name"));

                                String urlPhoto = snapshot.getString("url_photo");
                                if (!urlPhoto.equals("")) {
                                    imgRecentUserProfile.setBackgroundDrawable(null);
                                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(mContext);
                                    circularProgressDrawable.setStrokeWidth(5f);
                                    circularProgressDrawable.setCenterRadius(30f);
                                    circularProgressDrawable.start();
                                    GlideApp.with(mContext)
                                            .load(urlPhoto)
                                            .apply(GlideOptions.circleCropTransform())
                                            .placeholder(circularProgressDrawable)
                                            .into(imgRecentUserProfile);
                                }
                            }
                        }
                    });
        }

        @Override
        public void onClick(View v) {
            recentItemClickListener.setOnItemClick(mRecentListModel);
        }

        @Override
        public boolean onLongClick(View v) {
            recentItemClickListener.setOnItemLongClick(mRecentListModel);
            return false;
        }
    }

    public interface RecentItemClickListener {
        void setOnItemClick(RecentListModel model);

        boolean setOnItemLongClick(RecentListModel model);
    }

}
