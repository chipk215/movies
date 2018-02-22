package com.keyeswest.movies.ui;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.keyeswest.movies.DetailMovieActivity;
import com.keyeswest.movies.R;
import com.keyeswest.movies.models.Movie;

import org.junit.Before;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class DetailMovieActivityTest {

    @Rule
    public ActivityTestRule mActivityRule =
            new ActivityTestRule<>(DetailMovieActivity.class, false, false);

    private  Movie mMovie;

   @Before
   public  void initMovie(){
       mMovie = new Movie();
       mMovie.setId(337167);
       mMovie.setOriginalTitle("Fifty Shades Freed");
       mMovie.setOverview("Believing they have left behind shadowy...");
       mMovie.setPopularity(443.09653f);
       mMovie.setPosterImage(null);
       mMovie.setPosterPath("//jjPJ4s3DWZZvI4vw8Xfi4Vqa1Q8.jpg");
       mMovie.setReleaseDate("2018-02-07");
       mMovie.setTitle("Fifty Shades Freed");
       mMovie.setVideo(false);
       mMovie.setVoteAverage(6.8f);
       mMovie.setVoteCount(419);

   }


    @Test
    public void originalTitleTest(){

        Intent intent = DetailMovieActivity.newIntent(getTargetContext(),mMovie);
        mActivityRule.launchActivity(intent);

        // verify the DetailMovieActivity launches and the original title is correct
        onView(withId(R.id.original_title_tv)).check(matches(isDisplayed()));
        onView(withId(R.id.original_title_tv)).check(matches(withText(mMovie.getOriginalTitle())));

    }

    @Test
    public void duplicateTitleTest(){

        // the title field should not be displayed if it is the same as the Original Title
        Intent intent = DetailMovieActivity.newIntent(getTargetContext(),mMovie);
        mActivityRule.launchActivity(intent);

        onView(withId(R.id.movie_title_tv)).check(matches(not(isDisplayed())));

    }


    @Test
    public void differentTitleTest(){

        mMovie.setTitle("Test Title");

        // the title field should not be displayed if it is the same as the Original Title
        Intent intent = DetailMovieActivity.newIntent(getTargetContext(),mMovie);
        mActivityRule.launchActivity(intent);

        onView(withId(R.id.movie_title_tv)).check(matches(isDisplayed()));
        onView(withId(R.id.movie_title_tv)).check(matches(withText(mMovie.getTitle())));


    }


}
