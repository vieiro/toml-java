#!/usr/bin/env sh

#
# Converts all ".toml" files to equivalent ".json" files.
# See, for instance, https://github.com/woodruffw/toml2json

for A in *.toml
do
    B=`echo $A | sed -e 's/toml$/json/'`
    echo "Converting $A to $B..."
    toml2json $A -p > $B
done
