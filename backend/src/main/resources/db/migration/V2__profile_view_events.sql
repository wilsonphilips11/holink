CREATE TABLE profile_view_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    referrer VARCHAR(2048),
    user_agent VARCHAR(1024),
    ip_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profile_view_events_profile_id ON profile_view_events (profile_id);
CREATE INDEX idx_profile_view_events_created_at ON profile_view_events (created_at);
