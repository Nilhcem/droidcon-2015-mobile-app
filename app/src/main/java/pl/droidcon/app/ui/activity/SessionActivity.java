package pl.droidcon.app.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;
import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.api.Session;
import pl.droidcon.app.model.api.Speaker;
import pl.droidcon.app.model.db.RealmSchedule;
import pl.droidcon.app.ui.dialog.SpeakerDialog;
import pl.droidcon.app.ui.view.SpeakerList;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SessionActivity extends BaseActivity implements SpeakerList.SpeakerItemClickListener {

    private static final String TAG = SessionActivity.class.getSimpleName();

    private static final String SESSION_EXTRA = "session";

    public static void start(Context context, Session session) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(SESSION_EXTRA, session);
        context.startActivity(intent);
    }

    private Session session;

    @Bind(R.id.speaker_photos)
    ViewPager speakerPhotos;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.session_description)
    TextView description;
    @Bind(R.id.session_date)
    TextView date;
    @Bind(R.id.indicator)
    CircleIndicator indicator;
    @Bind(R.id.speakers)
    SpeakerList speakerListView;
    @Bind(R.id.favourite_button)
    FloatingActionButton favouriteButton;

    @Inject
    DatabaseManager databaseManager;

    private CompositeSubscription compositeSubscription;
    private FavouriteClickListener favouriteClickListener = new FavouriteClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_activity);
        DroidconInjector.get().inject(this);
        ButterKnife.bind(this);
        setupToolbarBack(toolbar);
        session = getIntent().getExtras().getParcelable(SESSION_EXTRA);
        compositeSubscription = new CompositeSubscription();
        fillDetails();
        checkIsFavourite();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    private void fillDetails() {
        setToolbarTitle(session.title);
        List<Speaker> speakersList = session.getSpeakersList();
        speakerPhotos.setAdapter(new SpeakerPhotosAdapter(this, speakersList));
        description.setText(session.description);
        date.setText(DateTimePrinter.toPrintableStringWithDay(session.date));
        indicator.setViewPager(speakerPhotos);
        if (speakersList.size() == 1) {
            indicator.setVisibility(View.INVISIBLE);
        }
        speakerListView.setSpeakers(speakersList, this);
        favouriteButton.setOnClickListener(favouriteClickListener);
    }

    @Override
    public void onSpeakerClicked(@NonNull Speaker speaker) {
        SpeakerDialog.newInstance(speaker).show(getSupportFragmentManager(), TAG);
    }

    private void checkIsFavourite() {
        Subscription subscription = databaseManager.isFavourite(session)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "favourite check exception", e);
                    }

                    @Override
                    public void onNext(Boolean isFavourite) {
                        setRightFloatingActionButtonAction(isFavourite);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void setRightFloatingActionButtonAction(boolean isFavourite) {
        if (isFavourite) {
            favouriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        } else {
            favouriteButton.setImageResource(R.drawable.ic_favorite_black_24dp);
        }
        favouriteClickListener.alreadyFavourite = isFavourite;
    }

    private void addToFavourites() {
        Subscription subscription = databaseManager.addToFavourite(session)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RealmSchedule>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "on completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error adding to favourites", e);
                    }

                    @Override
                    public void onNext(RealmSchedule realmSchedule) {
                        if (realmSchedule != null) {
                            setRightFloatingActionButtonAction(true);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void removeFromFavourites() {
        Subscription subscription = databaseManager.removeFromFavourite(session)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error removing from favourites", e);
                    }

                    @Override
                    public void onNext(Boolean removeResult) {
                        setRightFloatingActionButtonAction(!removeResult);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private class FavouriteClickListener implements View.OnClickListener {

        private boolean alreadyFavourite;

        @Override
        public void onClick(View v) {
            if (!alreadyFavourite) {
                addToFavourites();
            } else {
                removeFromFavourites();
            }
        }
    }

    private static class SpeakerPhotosAdapter extends PagerAdapter {

        private Context context;
        private List<Speaker> speakers;

        private SpeakerPhotosAdapter(Context context, List<Speaker> speakers) {
            this.context = context;
            this.speakers = speakers;
        }

        @Override
        public int getCount() {
            return speakers.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.speaker_pager_item, container, false);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.speaker_photo);
            Picasso.with(context)
                    .load(UrlHelper.url(speakers.get(position).imageUrl))
                    .into(imageView);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ViewGroup) object);
        }
    }
}