#!/usr/bin/env sh

count="${1?no count}"
size="${2?no size}"
rate="${3?no rate}"

gcc -o producer producer.c
seq 1 "$count" | parallel -j"$count" -n0 ./producer 127.0.0.1 9000 "$size" "$((1000000/"$rate"))"
