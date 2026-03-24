package com.termux.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.termux.R;
import com.termux.app.TermuxActivity;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 mViewPager;
    private Button mNextButton;
    private List<OnboardingStep> mSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        mSteps = new ArrayList<>();
        mSteps.add(new OnboardingStep("Welcome to Termux Next", "Experience the most powerful terminal for Android, now with a modern redesign and smart features.", R.drawable.banner));
        mSteps.add(new OnboardingStep("Command Suggestions", "Boost your productivity with intelligent history-based command suggestions and autocompletion.", R.drawable.ic_service_notification));
        mSteps.add(new OnboardingStep("Workspaces", "Organize your terminal sessions into workspaces. Pin and search through your active tasks with ease.", R.drawable.ic_new_session));
        mSteps.add(new OnboardingStep("Premium Customization", "Enjoy Material You dynamic colors and a fully customizable extra-keys toolbar.", R.drawable.ic_settings));

        mViewPager = findViewById(R.id.onboarding_view_pager);
        mNextButton = findViewById(R.id.onboarding_next_button);
        Button skipButton = findViewById(R.id.onboarding_skip_button);

        mViewPager.setAdapter(new OnboardingAdapter(mSteps));

        new TabLayoutMediator(findViewById(R.id.onboarding_indicator), mViewPager, (tab, position) -> {}).attach();

        mNextButton.setOnClickListener(v -> {
            if (mViewPager.getCurrentItem() < mSteps.size() - 1) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        skipButton.setOnClickListener(v -> finishOnboarding());

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == mSteps.size() - 1) {
                    mNextButton.setText("Get Started");
                } else {
                    mNextButton.setText("Next");
                }
            }
        });
    }

    private void finishOnboarding() {
        TermuxAppSharedPreferences preferences = TermuxAppSharedPreferences.build(this);
        if (preferences != null) {
            preferences.setFirstRun(false);
        }
        startActivity(new Intent(this, TermuxActivity.class));
        finish();
    }

    private static class OnboardingStep {
        String title;
        String description;
        int imageRes;

        OnboardingStep(String title, String description, int imageRes) {
            this.title = title;
            this.description = description;
            this.imageRes = imageRes;
        }
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        private final List<OnboardingStep> steps;

        OnboardingAdapter(List<OnboardingStep> steps) {
            this.steps = steps;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnboardingStep step = steps.get(position);
            holder.title.setText(step.title);
            holder.description.setText(step.description);
            holder.image.setImageResource(step.imageRes);
        }

        @Override
        public int getItemCount() {
            return steps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, description;
            ImageView image;

            ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.onboarding_title);
                description = view.findViewById(R.id.onboarding_description);
                image = view.findViewById(R.id.onboarding_image);
            }
        }
    }
}
