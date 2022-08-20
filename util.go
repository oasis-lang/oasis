package main

import (
	"errors"
)

func insert[T any](orig []T, index int, value T) ([]T, error) {
	if index < 0 {
		return nil, errors.New("Index cannot be less than 0")
	}

	if index >= len(orig) {
		return append(orig, value), nil
	}

	orig = append(orig[:index+1], orig[index:]...)
	orig[index] = value

	return orig, nil
}
