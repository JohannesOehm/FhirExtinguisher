/**
 * Vm = View Model
 */
export interface VmColumn {
    name: string,
    type: string,
    expression: string,
    subColumns?: VmSubColumn[]
}

export interface VmSubColumn {
    name: string,
    expression: string
};
