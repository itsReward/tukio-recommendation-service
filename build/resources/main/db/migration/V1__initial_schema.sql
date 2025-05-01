-- Path: src/main/resources/db/migration/V1__initial_schema.sql

-- User Activities table
CREATE TABLE IF NOT EXISTS user_activities (
                                               id BIGSERIAL PRIMARY KEY,
                                               user_id BIGINT NOT NULL,
                                               event_id BIGINT NOT NULL,
                                               activity_type VARCHAR(50) NOT NULL,
    rating INT CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5)),
    view_duration INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- User Preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
                                                id BIGSERIAL PRIMARY KEY,
                                                user_id BIGINT NOT NULL,
                                                category_id BIGINT NOT NULL,
                                                preference_score DOUBLE PRECISION NOT NULL,
                                                preferred_location VARCHAR(255),
    preferred_day_of_week INT CHECK (preferred_day_of_week IS NULL OR (preferred_day_of_week >= 1 AND preferred_day_of_week <= 7)),
    preferred_time_of_day VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, category_id)
    );

-- User Preference Tags table
CREATE TABLE IF NOT EXISTS user_preference_tags (
                                                    preference_id BIGINT NOT NULL REFERENCES user_preferences(id),
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (preference_id, tag)
    );

-- Event Similarities table
CREATE TABLE IF NOT EXISTS event_similarities (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  event_id1 BIGINT NOT NULL,
                                                  event_id2 BIGINT NOT NULL,
                                                  similarity_score DOUBLE PRECISION NOT NULL,
                                                  last_calculated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  UNIQUE (event_id1, event_id2)
    );

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_user_activities_user_id ON user_activities(user_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_event_id ON user_activities(event_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_activity_type ON user_activities(activity_type);
CREATE INDEX IF NOT EXISTS idx_user_activities_created_at ON user_activities(created_at);

CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_preferences_category_id ON user_preferences(category_id);
CREATE INDEX IF NOT EXISTS idx_user_preferences_score ON user_preferences(preference_score);

CREATE INDEX IF NOT EXISTS idx_user_preference_tags_tag ON user_preference_tags(tag);

CREATE INDEX IF NOT EXISTS idx_event_similarities_event_id1 ON event_similarities(event_id1);
CREATE INDEX IF NOT EXISTS idx_event_similarities_event_id2 ON event_similarities(event_id2);
CREATE INDEX IF NOT EXISTS idx_event_similarities_score ON event_similarities(similarity_score);