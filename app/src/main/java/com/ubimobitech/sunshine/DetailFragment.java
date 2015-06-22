package com.ubimobitech.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ubimobitech.sunshine.data.WeatherContract;
import com.ubimobitech.sunshine.data.WeatherContract.WeatherEntry;
import com.ubimobitech.sunshine.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String WEATHER_FORECAST_INTENT_EXTRA =
                "com.ubimobitech.sunshine.WEATHER_FORECAST_INTENT_EXTRA";
    public static final String ITEM_INDEX_INTENT_EXTRA =
            "com.ubimobitech.sunshine.ITEM_INDEX_INTENT_EXTRA";
    public static final String DETAIL_URI = "URI";

    private TextView mForecast;
    private CharSequence mForecastText;
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private Uri mUri;

    private ShareActionProvider mShareActionProvider;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_ID = 9;

    @InjectView(R.id.forecast_friendly_date) TextView mDayName;
    @InjectView(R.id.forecast_date) TextView mDateMonth;
    @InjectView(R.id.forecast_max_temp) TextView mMaxTemp;
    @InjectView(R.id.forecast_min_temp) TextView mMinTemp;
    @InjectView(R.id.forecast_icon) ImageView mForecastIcon;
    @InjectView(R.id.forecast_description) TextView mDescription;
    @InjectView(R.id.forecast_humidity) TextView mHumidity;
    @InjectView(R.id.forecast_pressure) TextView mPressure;
    @InjectView(R.id.forecast_wind) TextView mWindSpeed;

    /**
     * Create a new instance of DetailFragment, initialised to show
     * the text at index.
     */
    public static DetailFragment newInstance(int index) {
        DetailFragment f = new DetailFragment();

        Bundle args = new Bundle();
        args.putInt(ITEM_INDEX_INTENT_EXTRA, index);
        f.setArguments(args);

        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt(ITEM_INDEX_INTENT_EXTRA, 0);
    }

    public DetailFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.inject(this, view);

        setHasOptionsMenu(true);

        Bundle args = getArguments();

        if (args != null) {
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
        }

        return view;
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

        createShareForecastIntent();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Fragment#onCreateOptionsMenu(Menu, MenuInflater) Fragment.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu resource file.
        inflater.inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        createShareForecastIntent();
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;

        if (uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation,
                    date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    // Call to update the share intent
    private void createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastText + FORECAST_SHARE_HASHTAG);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(), mUri,
                    DETAIL_COLUMNS, null, null, null);
        }

        return null;
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.
     * <p/>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p/>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param cursor The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            int weatherId = cursor.getInt(COL_WEATHER_ID);
            long date = cursor.getLong(COL_WEATHER_DATE);

            mDayName.setText(Utils.getDayName(getActivity(), date));
            mDateMonth.setText(Utils.getFormattedMonthDay(getActivity(), date));

            mForecastIcon.setImageResource(Utils.getArtResourceForWeatherCondition(weatherId));

            mMaxTemp.setText(Utils.formatTemperature(getActivity(),
                    cursor.getDouble(COL_WEATHER_MAX_TEMP)));
            mMinTemp.setText(Utils.formatTemperature(getActivity(),
                    cursor.getDouble(COL_WEATHER_MIN_TEMP)));

            mDescription.setText(cursor.getString(COL_WEATHER_DESC));

            mHumidity.setText(getString(R.string.format_humidity, cursor.getDouble(COL_WEATHER_HUMIDITY)));
            mPressure.setText(getString(R.string.format_pressure, cursor.getDouble(COL_WEATHER_PRESSURE)));
            mWindSpeed.setText(Utils.getFormattedWind(getActivity(),
                    cursor.getFloat(COL_WEATHER_WIND_SPEED),
                    cursor.getFloat(COL_WEATHER_DEGREES)));

            createShareForecastIntent();
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
