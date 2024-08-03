use std::{env, thread};
use std::io::prelude::*;
use std::net::TcpStream;
use std::process::ExitCode;
use std::time::Duration;
use rand::RngCore;

fn main() -> ExitCode {
    let args: Vec<String> = env::args().collect();

    if args.len() < 5 {
        println!("Missing arguments! Usage: <addr> <port> <buffer size> <delay micros>\n");
        return ExitCode::FAILURE;
    }

    let mut stream = match TcpStream::connect(format!("{}:{}", args[1], args[2])) {
        Ok(s) => s,
        Err(err) => {
            eprintln!("Failed to connect: {}", err);
            return ExitCode::FAILURE;
        }
    };

    let mut buffer = match args[3].parse() {
        Ok(capacity) => Vec::with_capacity(capacity),
        Err(err) => {
            eprintln!("Failed to parse buffer size: {}", err);
            return ExitCode::FAILURE;
        }
    };
    let buffer = buffer.as_mut_slice();

    let delay = match args[4].parse() {
        Ok(delay_micros) => Duration::from_micros(delay_micros),
        Err(err) => {
            eprintln!("Failed to parse delay micros: {}", err);
            return ExitCode::FAILURE;
        }
    };

    rand::thread_rng().fill_bytes(buffer);

    loop {
        let mut offset = 0;
        let mut remaining_bytes = buffer.len();
        while remaining_bytes > 0 {
            match stream.write(&buffer[offset..remaining_bytes]) {
                Ok(bytes_written) => {
                    offset += bytes_written;
                    remaining_bytes -= bytes_written;
                }
                Err(error) => {
                    eprintln!("Failed to write data: {}", error);
                    return ExitCode::FAILURE;
                }
            }
        }
        thread::sleep(delay);
    }
}
