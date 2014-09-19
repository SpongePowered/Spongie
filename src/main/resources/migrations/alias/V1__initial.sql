CREATE TABLE ${tablePrefix}aliases (
    network VARCHAR(255) NOT NULL,
    channel VARCHAR(255) NOT NULL,
	alias VARCHAR(255) NOT NULL,
	command TEXT NOT NULL,
	PRIMARY KEY (network, channel, alias)
);

CREATE TABLE ${tablePrefix}inheritance (
    network VARCHAR(255) NOT NULL,
    channel VARCHAR(255) NOT NULL,
	target_network VARCHAR(255) NOT NULL,
    target_channel VARCHAR(255) NOT NULL,
	PRIMARY KEY (network, channel)
);