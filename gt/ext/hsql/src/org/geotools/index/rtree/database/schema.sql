create table rtrees_cat (
	rtree_name		varchar(20) not null primary key,
	min_entries		int not null,
	max_entries		int not null,
	split_alg		int not null
);

create table rtree (
	page_id			int not null primary key,
	fl_leaf			char(1) not null,
	blob_content	blob
);