alter table energia.user_terms
    add column if not exists event_type varchar(20);

update energia.user_terms
set event_type = case
    when revoked_at is not null then 'REVOKED'
    else 'ACCEPTED'
end
where event_type is null;

alter table energia.user_terms
    alter column event_type set not null;
