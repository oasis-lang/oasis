package main

type Tuple struct {
	Values []interface{}
}

func (tuple *Tuple) Append(value interface{}) {
	tuple.Values = append(tuple.Values, value)
}
