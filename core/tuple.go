package core

type Tuple struct {
	Values []any
}

func (tuple *Tuple) Append(value any) {
	tuple.Values = append(tuple.Values, value)
}
