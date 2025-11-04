-- Postgres + PostGIS base schema
-- Extensions
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Utility trigger to auto-update updated_at
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_proc WHERE proname = 'set_updated_at'
    ) THEN
        CREATE FUNCTION set_updated_at() RETURNS trigger AS $$
        BEGIN
            NEW.updated_at := NOW();
            RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;
    END IF;
END$$;

-- Table: bus_stops (정류장)
CREATE TABLE IF NOT EXISTS bus_stops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id UUID NOT NULL REFERENCES academies(id),
    name VARCHAR(255) NOT NULL,
    geom GEOGRAPHY(Point,4326) NOT NULL,
    photo_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Spatial index for bus_stops
CREATE INDEX IF NOT EXISTS idx_bus_stops_geom ON bus_stops USING GIST (geom);

-- Auto-update trigger
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_bus_stops_set_updated_at'
    ) THEN
        CREATE TRIGGER trg_bus_stops_set_updated_at
        BEFORE UPDATE ON bus_stops
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;
END$$;

-- Table: routes (노선)
-- Note: Using JSONB for Kakao Navi response, name as varchar(255), total_time as time
CREATE TABLE IF NOT EXISTS routes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id UUID NOT NULL REFERENCES academies(id),
    nav_response JSONB NOT NULL,
    name VARCHAR(255) NOT NULL,
    total_time TIME NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_routes_set_updated_at'
    ) THEN
        CREATE TRIGGER trg_routes_set_updated_at
        BEFORE UPDATE ON routes
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;
END$$;

-- Single UUID PK with UNIQUE(route_id, stop_id); maintains insertion order via stop_order
CREATE TABLE IF NOT EXISTS route_stops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_id UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    stop_id UUID NOT NULL REFERENCES bus_stops(id) ON DELETE RESTRICT,
    stop_order INT NOT NULL,
    start_to_arrive_time TIME NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_route_stop UNIQUE (route_id, stop_id)
);

CREATE INDEX IF NOT EXISTS idx_route_stops_order ON route_stops(route_id, stop_order);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_route_stops_set_updated_at'
    ) THEN
        CREATE TRIGGER trg_route_stops_set_updated_at
        BEFORE UPDATE ON route_stops
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;
END$$;

-- Table: schedules (스케줄)
-- boarding_status as enum-like via CHECK constraint
CREATE TABLE IF NOT EXISTS schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_id UUID NOT NULL REFERENCES routes(id),
    name VARCHAR(255) NOT NULL,
    repeat_days INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    boarding_status VARCHAR(20) NOT NULL DEFAULT 'PICK_UP',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_schedules_boarding_status CHECK (boarding_status IN ('PICK_UP','DROP_OFF'))
);

CREATE INDEX IF NOT EXISTS idx_schedules_route ON schedules(route_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_schedules_set_updated_at'
    ) THEN
        CREATE TRIGGER trg_schedules_set_updated_at
        BEFORE UPDATE ON schedules
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;
END$$;

-- Table: student_schedules (학생-스케줄 매핑)
-- Primary key across (student_id, schedule_id, date)
CREATE TABLE IF NOT EXISTS student_schedules (
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    schedule_id UUID NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    boarding_status VARCHAR(20) NOT NULL,
    planned_stop_id UUID NOT NULL REFERENCES bus_stops(id),
    planned_time TIME NOT NULL,
    schedule_status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_student_schedules PRIMARY KEY (student_id, schedule_id, date),
    CONSTRAINT ck_student_schedules_boarding_status CHECK (boarding_status IN ('PICK_UP','DROP_OFF')),
    CONSTRAINT ck_student_schedules_status CHECK (schedule_status IN ('RESERVED','CANCELED'))
);

CREATE INDEX IF NOT EXISTS idx_student_schedules_sched ON student_schedules(schedule_id);
CREATE INDEX IF NOT EXISTS idx_student_schedules_stop ON student_schedules(planned_stop_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_student_schedules_set_updated_at'
    ) THEN
        CREATE TRIGGER trg_student_schedules_set_updated_at
        BEFORE UPDATE ON student_schedules
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;
END$$;
