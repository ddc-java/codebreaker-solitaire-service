create table game
(
    game_id      UUID         not null,
    created      timestamp    not null,
    external_key UUID         not null,
    length       integer      not null check (length <= 20 AND length >= 1),
    pool         varchar(255) not null,
    code_text    varchar(20)  not null,
    primary key (game_id)
);

create table guess
(
    guess_id      UUID        not null,
    created       timestamp   not null,
    exact_matches integer     not null,
    external_key  UUID        not null,
    near_matches  integer     not null,
    guess_text    varchar(20) not null,
    game_id       UUID        not null,
    primary key (guess_id)
);

create index IDXlk7h4xhf32khhkbqqlpw3h6c6 on game (created);

alter table game
    add constraint UK_h5ly13atfey58ueo3eccgihug unique (external_key);

create index IDX4xl15u97wgd6b6ji19yfqgdjr on guess (created);

alter table guess
    add constraint UK_91c9f1d56yrtyaj69sprv3ppx unique (external_key);

alter table guess
    add constraint FK17wrv62yn4umhcoh8y608l16d foreign key (game_id) references game;
