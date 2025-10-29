CREATE TABLE calc_profiles (
    id SERIAL PRIMARY KEY,
    code VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    standard_references TEXT,
    lighting_voltage_drop_limit NUMERIC(5,2) NOT NULL,
    general_voltage_drop_limit NUMERIC(5,2) NOT NULL
);

CREATE TABLE projects (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    client_name VARCHAR(255),
    address VARCHAR(255),
    description TEXT,
    voltage_system_type VARCHAR(32) NOT NULL,
    nominal_voltage NUMERIC(10,2),
    cos_phi NUMERIC(5,3),
    efficiency NUMERIC(5,3),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calc_profile_id INTEGER REFERENCES calc_profiles(id)
);

CREATE TABLE panels (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    panel_type VARCHAR(32) NOT NULL,
    din_rail_count INTEGER,
    supply_source VARCHAR(255),
    notes TEXT
);

CREATE TABLE circuits (
    id SERIAL PRIMARY KEY,
    panel_id INTEGER NOT NULL REFERENCES panels(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    load_type VARCHAR(255),
    demand_power_kw NUMERIC(10,3),
    voltage NUMERIC(10,3),
    cos_phi NUMERIC(5,3),
    efficiency NUMERIC(5,3),
    phase_type VARCHAR(32) NOT NULL,
    length_m NUMERIC(10,3),
    conductor_cross_section NUMERIC(10,3),
    conductor_material VARCHAR(32),
    conductor_insulation VARCHAR(32),
    installation_method VARCHAR(64),
    grouping_factor_count INTEGER,
    ambient_temperature NUMERIC(5,2),
    is_lighting BOOLEAN DEFAULT FALSE,
    protective_device_curve VARCHAR(8),
    protective_device_rating NUMERIC(10,3),
    design_current NUMERIC(10,3),
    breaker_current NUMERIC(10,3),
    iz NUMERIC(10,3),
    voltage_drop NUMERIC(10,3),
    conductor_colors TEXT
);

CREATE TABLE components (
    id SERIAL PRIMARY KEY,
    panel_id INTEGER REFERENCES panels(id) ON DELETE CASCADE,
    circuit_id INTEGER REFERENCES circuits(id) ON DELETE SET NULL,
    component_type VARCHAR(32) NOT NULL,
    manufacturer VARCHAR(255),
    model VARCHAR(255),
    reference_standard VARCHAR(128),
    pole_count INTEGER,
    rating NUMERIC(10,3),
    curve VARCHAR(8),
    position_index INTEGER
);

CREATE TABLE cables (
    id SERIAL PRIMARY KEY,
    circuit_id INTEGER NOT NULL REFERENCES circuits(id) ON DELETE CASCADE,
    designation VARCHAR(255),
    cross_section NUMERIC(10,3),
    material VARCHAR(32),
    insulation VARCHAR(32),
    conductor_count INTEGER,
    color VARCHAR(32),
    length NUMERIC(10,3)
);

CREATE TABLE wiring_routes (
    id SERIAL PRIMARY KEY,
    circuit_id INTEGER REFERENCES circuits(id) ON DELETE CASCADE,
    project_id INTEGER REFERENCES projects(id) ON DELETE CASCADE,
    origin_point VARCHAR(255),
    destination_point VARCHAR(255),
    routing_notes TEXT,
    length NUMERIC(10,3),
    conduit_type VARCHAR(64),
    labeling VARCHAR(255)
);

CREATE TABLE earthing_systems (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    configuration VARCHAR(255),
    target_resistance NUMERIC(10,3),
    improvement_actions TEXT
);

CREATE TABLE measurements (
    id SERIAL PRIMARY KEY,
    earthing_system_id INTEGER NOT NULL REFERENCES earthing_systems(id) ON DELETE CASCADE,
    resistance NUMERIC(10,3) NOT NULL,
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    instrument VARCHAR(255),
    notes TEXT
);

CREATE TABLE materials (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    unit VARCHAR(32) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit_cost NUMERIC(12,2),
    reference_standard VARCHAR(128),
    supplier VARCHAR(255)
);

CREATE TABLE checklist_items (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    evidence_url TEXT,
    notes TEXT
);

CREATE INDEX idx_panels_project ON panels(project_id);
CREATE INDEX idx_circuits_panel ON circuits(panel_id);
CREATE INDEX idx_components_panel ON components(panel_id);
CREATE INDEX idx_components_circuit ON components(circuit_id);
CREATE INDEX idx_cables_circuit ON cables(circuit_id);
CREATE INDEX idx_wiring_routes_project ON wiring_routes(project_id);
CREATE INDEX idx_measurements_earthing ON measurements(earthing_system_id);
CREATE INDEX idx_materials_project ON materials(project_id);
CREATE INDEX idx_checklist_project ON checklist_items(project_id);
