#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <stdbool.h>

#define parse_int(to, from) \
    to = strtol((from), NULL, 10); \
    if (errno != 0) { \
        printf("Failed to parse int from " #from ": %s\n", strerror(errno)); \
        return 1; \
    }

#define INITIAL_RANDOM_BYTES 42
#define MIN(x, y) (((x) < (y)) ? (x) : (y))

int main(int argc, char** argv) {

    if (argc < 5) {
        printf("Missing arguments! Usage: <addr> <port> <buffer size> <delay micros>\n");
        return 1;
    }

    struct sockaddr_in addr = {
        .sin_family = AF_INET,
    };

    if (!inet_pton(AF_INET, argv[1], &addr.sin_addr)) {
        printf("Failed to convert IP to addr: %s\n", strerror(errno));
        return 2;
    }

    parse_int(addr.sin_port, argv[2]);
    addr.sin_port = htons(addr.sin_port);

    int fd = socket(AF_INET, SOCK_STREAM, 0);
    if (fd < 0) {
        printf("Failed to create socket: %s\n", strerror(errno));
        return 4;
    }


    if (connect(fd, (struct sockaddr*) &addr, sizeof(addr))) {
        printf("Failed to connect: %s\n", strerror(errno));
        return 5;
    }

    int buffer_size;
    parse_int(buffer_size, argv[3]);

    char* buffer = malloc(buffer_size);
    if (buffer == NULL) {
        printf("Failed to allocate buffer: %s\n", strerror(errno));
        return 6;
    }

    srand(time(NULL));

    for (int i = 0; i < INITIAL_RANDOM_BYTES; i++) {
        buffer[i] = (char)(rand() % 255);
        printf("%d = %d\n", i, buffer[i]);
    }

    int remaining = buffer_size - INITIAL_RANDOM_BYTES;
    char* offset = buffer + INITIAL_RANDOM_BYTES;
    while (remaining > 0) {
        int amount = MIN(remaining, INITIAL_RANDOM_BYTES);
        memcpy(offset, buffer, amount);
        offset += amount;
        remaining -= amount;
    }

    int delay;
    parse_int(delay, argv[4]);

    while (true) {
        int remaining_bytes = buffer_size;
        while (remaining_bytes > 0) {
            int bytes_written = write(fd, buffer, remaining_bytes);
            printf("Bytes written: %d\n", bytes_written);
            remaining_bytes -= bytes_written;
        }
        usleep(delay);
    }

}
