#! /usr/bin/env bash

# Quick and dirty "benchmark" script :P

GRAAL_NATIVE_EXEC='./jordle'
JVM_EXEC='bin/jordle'

ALGO='entropy'
WARMUPS=2

hyperfine -w ${WARMUPS} --export-json bench.json \
  -n "${ALGO}-1" "${JVM_EXEC} -a ${ALGO} 1" \
  -n "${ALGO}-native-1" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 1" \
  -n "${ALGO}-2" "${JVM_EXEC} -a ${ALGO} 2" \
  -n "${ALGO}-native-2" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 2" \
  -n "${ALGO}-4" "${JVM_EXEC} -a ${ALGO} 4" \
  -n "${ALGO}-native-4" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 4" \
  -n "${ALGO}-8" "${JVM_EXEC} -a ${ALGO} 8" \
  -n "${ALGO}-native-8" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 8" \
  -n "${ALGO}-16" "${JVM_EXEC} -a ${ALGO} 16" \
  -n "${ALGO}-native-16" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 16" \
  -n "${ALGO}-32" "${JVM_EXEC} -a ${ALGO} 32" \
  -n "${ALGO}-native-32" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 32" \
  -n "${ALGO}-64" "${JVM_EXEC} -a ${ALGO} 64" \
  -n "${ALGO}-native-64" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 64" \
  -n "${ALGO}-128" "${JVM_EXEC} -a ${ALGO} 128" \
  -n "${ALGO}-native-128" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 128" \
  -n "${ALGO}-256" "${JVM_EXEC} -a ${ALGO} 256" \
  -n "${ALGO}-native-256" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 256" \
  -n "${ALGO}-512" "${JVM_EXEC} -a ${ALGO} 512" \
  -n "${ALGO}-native-512" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 512" \
  -n "${ALGO}-1024" "${JVM_EXEC} -a ${ALGO} 1024" \
  -n "${ALGO}-native-1024" "${GRAAL_NATIVE_EXEC} -a ${ALGO} 1024"
